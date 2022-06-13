load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "model",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/settings",
    ],
    visibility = ["//visibility:public"],
)
