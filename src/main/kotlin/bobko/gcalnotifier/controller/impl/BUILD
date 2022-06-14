load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "impl",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/controller",
        "//src/main/kotlin/bobko/gcalnotifier/model",
        "//src/main/kotlin/bobko/gcalnotifier/view",
    ],
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "ControllerTest",
    srcs = ["ControllerTest.kt"],
    deps = [
        ":impl",
        "//src/main/kotlin/bobko/gcalnotifier/test",
    ],
)
