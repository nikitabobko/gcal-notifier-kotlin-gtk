import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest
import java.io.PrintWriter
import com.adarshr.gradle.testlogger.theme.ThemeType

val mainClassName = "bobko.gcalnotifier.MainKt"
val appName = "gcal-notifier-kotlin-gtk"
val appVersion = rootProject.file("src/main/resources/version.txt").readText().trim()

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
  }
}

plugins {
  kotlin("jvm") version "1.5.0"
  id("co.riiid.gradle") version "0.4.2" // gradle-github-plugin https://github.com/riiid/gradle-github-plugin
  id("com.adarshr.test-logger") version "1.7.0" // https://github.com/radarsh/gradle-test-logger-plugin
  id("org.jetbrains.kotlin.plugin.allopen") version "1.5.0"
}

allOpen {
  annotation("bobko.gcalnotifier.injector.InjectorAllOpen")
}

repositories {
  mavenCentral()
  jcenter()
  flatDir {
    dirs("/usr/share/java/")
  }
  maven {
    setUrl("https://dl.bintray.com/bobko/kotlin-ref-delegation")
  }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("ru.nikitabobko.kotlin.refdelegation:kotlin-ref-delegation:1.1.2")
  implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
  implementation("com.google.apis:google-api-services-calendar:v3-rev402-1.25.0")
  implementation("com.google.code.gson:gson:2.8.6")
  implementation(group = "", name = "gtk")

  // Test dependencies
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.0")
  testImplementation("org.mockito:mockito-core:3.2.4")
}

val jar = tasks.getByName("jar", type = Jar::class) {
  manifest {
    attributes(mapOf("Main-Class" to mainClassName))
  }
  from(
    configurations.runtimeClasspath.get()
      .filter { !it.name.contains("gtk") } // Don't pack gtk.jar https://github.com/nikitabobko/gcal-notifier-kotlin-gtk/issues/1
      .map { if (it.isDirectory) it else zipTree(it) }
  )
}

testlogger {
  theme = ThemeType.STANDARD
  showExceptions = true
  showStackTraces = true
  showFullStackTraces = false
  showCauses = true
  slowThreshold = 2000
  showSummary = true
  showSimpleNames = false
  showPassed = true
  showSkipped = true
  showFailed = true
  showStandardStreams = true
  showPassedStandardStreams = true
  showSkippedStandardStreams = true
  showFailedStandardStreams = true
}

task("runJar", type = Exec::class) {
  group = "Run"
  description = "Run compiled jar."
  setDependsOn(listOf("jar"))
  commandLine = listOf("java", "-cp", "${jar.archiveFile.get().asFile}:/usr/share/java/gtk.jar", mainClassName)
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
  testLogging.showStandardStreams = true
}

open class BashExec : DefaultTask() {
  var command: String? = null

  @TaskAction
  fun exec() {
    val command: String = command ?: throw Exception("Please specify command")
    command.split("\n").map { it.trim() }.filter { it != "" }.forEach { commandUnit ->
      println("> $commandUnit")
      val process: Process = Runtime.getRuntime().exec(arrayOf("bash", "-c", commandUnit))
      process.inputStream.bufferedReader().use {
        while (true) {
          println(it.readLine() ?: break)
        }
      }
      process.errorStream.bufferedReader().use {
        while (true) {
          System.err.println(it.readLine() ?: break)
        }
      }
      val exitCode: Int = process.waitFor()
      if (exitCode != 0) {
        error("\"$command\" exited with non-zero exit code: $exitCode")
      }
    }
  }
}

fun sha256(file: File): String {
  val bytes = file.readBytes()
  val md = MessageDigest.getInstance("SHA-256")
  val digest = md.digest(bytes)
  return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun String.exec(): String {
  val process: Process = Runtime.getRuntime().exec(arrayOf("bash", "-c", this))
  val output: String = process.inputStream.bufferedReader().use { it.readText() }
  val exitCode: Int = process.waitFor()
  if (exitCode != 0) {
    error("\"$this\" exited with non-zero exit code: $exitCode")
  }
  return output
}

/**
 * The same as [org.gradle.api.Task#setDependsOn] but also set `this.inputs`
 * to `dependency.output` for any `dependency` in `dependencies`
 */
var Task.smartDependsOn: Iterable<Any>
  set(dependencies) {
    dependencies
      .map { if (it is String) tasks.getByName(it) else it }
      .also { deps -> check(deps.all { it is Task }) { "Only Strings and Tasks are supported" } }
      .filterIsInstance<Task>()
      .forEach { inputs.files(it.outputs.files) }
    setDependsOn(dependencies)
  }
  get() = this.dependsOn

val assembledInstaller = file("build/installer")
task("assemblyInstallerDir", BashExec::class) {
  smartDependsOn = listOf(jar)

  val icon = file("src/main/resources/icon.png")
  val installerResDir = file("distribution/installer")
  inputs.files(icon)
  inputs.dir(installerResDir)
  outputs.dir(assembledInstaller)

  command = """
    mkdir -p $assembledInstaller
    cp $installerResDir/* $assembledInstaller
    cp $icon $assembledInstaller
    cp ${jar.archiveFile.get().asFile} $assembledInstaller
  """.trimIndent()
}

val tarFile = file("build/tar/$appName-v$appVersion.tar")
task("tar", BashExec::class) {
  group = "Distribution"
  description = "Build tar archive for distribution."
  smartDependsOn = listOf("assemblyInstallerDir")

  outputs.file(tarFile)

  val tmpDir = file("build/tmp/tar/$appName-v$appVersion")
  command = """
    rm -rf $tmpDir
    mkdir -p ${tmpDir.parent}
    cp -r $assembledInstaller $tmpDir
    mkdir -p ${tarFile.parent}
    tar -cf $tarFile -C ${tmpDir.parent} ${tmpDir.name}
  """.trimIndent()
}

val debFile = file("build/deb/$appName-v$appVersion.deb")
task("deb", BashExec::class) {
  group = "Distribution"
  description = "Build deb archive for distribution."
  smartDependsOn = listOf("assemblyInstallerDir")
  val tmpDir = file("build/tmp/deb")
  val debianDistributionResDir = file("distribution/debian")

  inputs.dir(debianDistributionResDir)
  outputs.file(debFile)

  command = """
    rm -rf $tmpDir
    mkdir -p $tmpDir
    $assembledInstaller/install.sh $tmpDir
    mkdir -p $tmpDir/DEBIAN
    cat ${debianDistributionResDir}/control > $tmpDir/DEBIAN/control
    echo 'Version: $appVersion' >> $tmpDir/DEBIAN/control
    mkdir -p ${debFile.parent}
    dpkg-deb --build $tmpDir $debFile
  """.trimIndent()
}

task("allDistributionArchives") {
  group = "Distribution"
  description = "Build all distribution archives."
  smartDependsOn = listOf("tar", "deb")
}

val pkgbuildFile = buildDir.resolve("archlinux").resolve("PKGBUILD")
task("pkgbuild") {
  group = "Distribution"
  description = "Generates ArchLinux's PKGBUILD file."

  smartDependsOn = listOf("tar")
  outputs.file(pkgbuildFile)

  doLast {
    val d = "$"
    val pkgbuildFileContent = """
      # Maintainer: Nikita Bobko <echo bmlraXRhYm9ia29AZ21haWwuY29tCg== | base64 -d>

      pkgname=gcal-notifier-kotlin-gtk
      pkgver=${appVersion}
      pkgrel=1
      pkgdesc='Simple Google Calendar notifier for Linux written in Kotlin using GTK lib'
      arch=('x86_64' 'i686')
      url='https://github.com/nikitabobko/${appName}'
      license=('GPL')
      depends=('java-gnome-bin' 'java-runtime>=8' 'libnotify' 'librsvg')
      source=("https://github.com/nikitabobko/${appName}/releases/download/v$d{pkgver//_/-}/${appName}-v$d{pkgver//_/-}.tar")
      sha256sums=("${sha256(tarFile)}")

      package() {
        cd $d{srcdir}/${appName}-v$d{pkgver}
        ./install.sh ${d}pkgdir/
      }
    """.trimIndent()
    pkgbuildFile.delete()
    PrintWriter(pkgbuildFile).use { it.println(pkgbuildFileContent) }
  }
}

github {
  owner = "nikitabobko"
  repo = appName
  token = File("secrets/github_access_token.txt").takeIf { it.exists() }?.readText()?.trim() ?: "..."
  tagName = "v${appVersion}"
  body = "git log -1 --pretty=%B".exec().trim()
  setAssets(tarFile.absolutePath, debFile.absolutePath)
}

tasks.getByName("githubRelease") {
  smartDependsOn = listOf("tar", "deb")
}
