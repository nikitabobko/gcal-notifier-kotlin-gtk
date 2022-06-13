load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "impl",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/controller",
        "//src/main/kotlin/bobko/gcalnotifier/model",
        "//src/main/kotlin/bobko/gcalnotifier/view",
    ],
    visibility = ["//visibility:public"],
)