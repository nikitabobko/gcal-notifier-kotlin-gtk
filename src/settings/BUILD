load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "settings",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/util",
        artifact("org.jetbrains.kotlin:kotlin-reflect")
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "SettingsFormatParserTest",
    test_class = "bobko.gcalnotifier.settings.SettingsFormatParserTest",
    srcs = ["SettingsFormatParserTest.kt"],
    deps = [
        ":settings",
        "//test-util",
    ],
)

kt_jvm_test(
    name = "SettingsTest",
    test_class = "bobko.gcalnotifier.settings.SettingsTest",
    srcs = ["SettingsTest.kt"],
    deps = [
        ":settings",
        "//src/util",
        "//test-util",
    ],
)
