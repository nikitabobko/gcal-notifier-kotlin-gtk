load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_test(
    name = "SettingsFormatParserTest",
    srcs = ["SettingsFormatParserTest.kt"],
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/settings",
        "//src/main/kotlin/bobko/gcalnotifier/util",
        artifact("org.jetbrains.kotlin:kotlin-test-junit"),
        artifact("org.mockito:mockito-core"),
    ],
)

kt_jvm_test(
    name = "SettingsTest",
    srcs = ["SettingsTest.kt"],
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/settings",
        "//src/main/kotlin/bobko/gcalnotifier/util",
        artifact("org.jetbrains.kotlin:kotlin-test-junit"),
        artifact("org.mockito:mockito-core"),
    ],
)
