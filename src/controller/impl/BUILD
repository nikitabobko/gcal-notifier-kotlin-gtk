load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "impl",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/controller",
        "//src/model",
        "//src/view",
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "ControllerTest",
    test_class = "bobko.gcalnotifier.controller.impl.ControllerTest",
    srcs = ["ControllerTest.kt"],
    deps = [
        ":impl",
        "//test-util",
    ],
)
