load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "impl",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/view",
        "//src/main/kotlin/bobko/gcalnotifier/model",
    ],
    visibility = ["//visibility:public"],
)