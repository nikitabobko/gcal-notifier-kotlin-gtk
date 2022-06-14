load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "model",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/settings",
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "MyEventTest",
    test_class = "bobko.gcalnotifier.model.MyEventTest",
    srcs = ["MyEventTest.kt"],
    deps = [
        ":model",
        "//test-util",
    ],
)
