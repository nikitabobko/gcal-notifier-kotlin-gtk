load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "main",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/injector",
    ],
    visibility = ["//visibility:public"],
)