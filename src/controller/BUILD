load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "controller",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/model",
        "//src/support",
        "//src/util",
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "EventReminderTrackerTest",
    test_class = "bobko.gcalnotifier.controller.EventReminderTrackerTest",
    srcs = ["EventReminderTrackerTest.kt"],
    deps = [
        ":controller",
        "//test-util",
    ],
)
