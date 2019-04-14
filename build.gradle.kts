import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
        set("kotlin_version", "1.3.21")
    }

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra.get("kotlin_version")}")
    }
}

plugins {
    id("java")
    kotlin("jvm") version "1.3.21"
    id("application")
}

group = "ru.nikitabobko"
version = rootProject.file("src/main/resources/version.txt").readText()

application {
    mainClassName = "ru.nikitabobko.gcalnotifier.MainKt"
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
    compile("ru.nikitabobko.kotlin.refdelegation:kotlin-ref-delegation:1.0")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:${extra.get("kotlin_version")}")
    testCompile("org.mockito:mockito-core:2.1.0")
    compile("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    compile("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0")
    compile("com.google.code.gson:gson:2.8.0")
    compile(group = "", name = "gtk")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
    from(configurations.compile.filter { !it.name.contains("gtk") }.map {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        return@map if (it.isDirectory) it else zipTree(it)
    })
}

task("runJar", type = Exec::class) {
    executable("java")
    args("-cp", "$jar.archivePath:/usr/share/java/gtk.jar", application.mainClassName)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    testLogging.showStandardStreams = true
}
