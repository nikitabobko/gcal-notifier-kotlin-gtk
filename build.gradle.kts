import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClassName = "ru.nikitabobko.gcalnotifier.MainKt"
val appName = "gcal-notifier-kotlin-gtk"
val appVersion = rootProject.file("src/main/resources/version.txt").readText().trim()

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.21")
    }
}

plugins {
    id("java")
    kotlin("jvm") version "1.3.21"
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
    compile("ru.nikitabobko.kotlin.refdelegation:kotlin-ref-delegation:1.1.2")
    compile("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    compile("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0")
    compile("com.google.code.gson:gson:2.8.0")
    compile(group = "", name = "gtk")

    // Test dependencies
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.3.21")
    testCompile("org.mockito:mockito-core:2.1.0")
}

val jar = tasks.getByName("jar", type = Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to mainClassName))
    }
    from(configurations.compile.filter { !it.name.contains("gtk") }.map {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        return@map if (it.isDirectory) it else zipTree(it)
    })
}

task("runJar", type = Exec::class) {
    group = "Run"
    description = "Run compiled jar."
    setDependsOn(listOf("jar"))
    commandLine = listOf("java", "-cp", "${jar.archivePath}:/usr/share/java/gtk.jar", mainClassName)
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
                throw RuntimeException("""
                    "$command" exited with non-zero exit code: $exitCode
                """.trimIndent())
            }
        }
    }
}

val assembledInstaller = file("build/installer")
task("assemblyInstallerDir", BashExec::class) {
    setDependsOn(listOf("jar"))
    command = """
        mkdir -p $assembledInstaller
        cp distribution/installer/* $assembledInstaller
        cp src/main/resources/icon.png $assembledInstaller
        cp ${jar.archivePath} $assembledInstaller
    """.trimIndent()
}

val tarFile = file("build/tar/$appName-v$appVersion.tar")
task("tar", BashExec::class) {
    group = "Distribution"
    description = "Build tar archive for distribution."
    setDependsOn(listOf("assemblyInstallerDir"))
    val tmpDir = file("build/tmp/$appName-v$appVersion")
    command = """
        rm -rf $tmpDir
            cp -r $assembledInstaller $tmpDir
            mkdir -p ${tarFile.parent}
            tar -cf $tarFile -C build/tmp/ ${tmpDir.name}
        rm -rf $tmpDir
    """.trimIndent()
}

val debFile = file("build/deb/$appName-v$appVersion.deb")
task("deb", BashExec::class) {
    group = "Distribution"
    description = "Build deb archive for distribution."
    setDependsOn(listOf("assemblyInstallerDir"))
    val tmpDir = file("build/tmp/deb")
    command = """
        rm -rf $tmpDir
            mkdir -p $tmpDir
            $assembledInstaller/install.sh $tmpDir
            mkdir -p $tmpDir/DEBIAN
            cat distribution/debian/control > $tmpDir/DEBIAN/control
            echo 'Version: $appVersion' >> $tmpDir/DEBIAN/control
            mkdir -p ${debFile.parent}
            dpkg-deb --build $tmpDir $debFile
        rm -rf $tmpDir
    """.trimIndent()
}
