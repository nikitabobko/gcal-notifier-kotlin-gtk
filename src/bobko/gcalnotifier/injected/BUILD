load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "injected",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    visibility = ["//src/bobko/gcalnotifier/injector:__pkg__"],
)
