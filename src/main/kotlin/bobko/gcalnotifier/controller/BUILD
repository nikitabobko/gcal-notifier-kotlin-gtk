load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "controller",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/model",
        "//src/main/kotlin/bobko/gcalnotifier/support",
        artifact("ru.nikitabobko.kotlin.refdelegation:kotlin-ref-delegation"),
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "EventReminderTrackerTest",
    srcs = ["EventReminderTrackerTest.kt"],
    deps = [
        ":controller",
        "//src/main/kotlin/bobko/gcalnotifier/test",
    ],
)
