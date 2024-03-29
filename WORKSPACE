workspace(name = "gcal-notifier")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_kotlin_version = "1.5.0"
rules_kotlin_sha = "12d22a3d9cbcf00f2e2d8f0683ba87d3823cb8c7f6837568dd7e48846e023307"

rules_jvm_external_tag = "4.2"
rules_jvm_external_sha = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

kotlin_version = "1.7.0"
kotlin_sha = "f5216644ad81571e5db62ec2322fe07468927bda40f51147ed626a2884b55f9a"

rules_pkg_version = "0.7.0"
rules_pkg_sha = "8a298e832762eda1830597d64fe7db58178aa84cd5926d76d5b744d6558941c2"

# rules_pkg
http_archive(
    name = "rules_pkg",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (rules_pkg_version, rules_pkg_version),
        "https://github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (rules_pkg_version, rules_pkg_version),
    ],
    sha256 = rules_pkg_sha
)
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# rules_jvm_external
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % rules_jvm_external_tag,
    sha256 = rules_jvm_external_sha,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % rules_jvm_external_tag,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")
rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
maven_install(
    artifacts = [
        "org.jetbrains.kotlin:kotlin-test-junit:%s" % kotlin_version,
        "org.jetbrains.kotlin:kotlin-reflect:%s" % kotlin_version,
        "org.mockito:mockito-core:3.2.4",
        "com.google.oauth-client:google-oauth-client-jetty:1.23.0",
        "com.google.apis:google-api-services-calendar:v3-rev402-1.25.0",
        "com.google.code.gson:gson:2.8.6",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    maven_install_json = "//:third_party/maven_install.json",
    fetch_sources = True,
    fail_if_repin_required = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()

# /usr/share/java
new_local_repository(
    name = "usr_share_java",
    path = "/usr/share/java/",
    build_file = "third_party/gtk.BUILD",
)

# Kotlin
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % rules_kotlin_version],
    sha256 = rules_kotlin_sha,
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")
kotlin_repositories(
    compiler_release = kotlinc_version(
        release = kotlin_version,
        sha256 = kotlin_sha
    )
)

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")
kt_register_toolchains()
