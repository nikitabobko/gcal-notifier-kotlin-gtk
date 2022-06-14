load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "view",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "@usr_share_java//:gtk",
        "//src/controller",
    ],
    visibility = ["//visibility:public"],
)