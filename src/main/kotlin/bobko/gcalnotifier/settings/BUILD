load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "settings",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/util",
        artifact("org.jetbrains.kotlin:kotlin-reflect")
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "SettingsFormatParserTest",
    srcs = ["SettingsFormatParserTest.kt"],
    deps = [
        ":settings",
        "//src/main/kotlin/bobko/gcalnotifier/test",
    ],
)

kt_jvm_test(
    name = "SettingsTest",
    srcs = ["SettingsTest.kt"],
    deps = [
        ":settings",
        "//src/main/kotlin/bobko/gcalnotifier/util",
        "//src/main/kotlin/bobko/gcalnotifier/test",
    ],
)
