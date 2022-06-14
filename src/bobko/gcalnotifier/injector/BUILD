load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "injector",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/bobko/gcalnotifier/controller",
        "//src/bobko/gcalnotifier/controller/impl",
        "//src/bobko/gcalnotifier/injected",
        "//src/bobko/gcalnotifier/util",
        "//src/bobko/gcalnotifier/view",
        "//src/bobko/gcalnotifier/view/gtk",
    ],
    visibility = ["//src/bobko/gcalnotifier:__pkg__"],
)