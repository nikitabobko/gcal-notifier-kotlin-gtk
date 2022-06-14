load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "injector",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/controller",
        "//src/controller/impl",
        "//src/injected",
        "//src/util",
        "//src/view",
        "//src/view/gtk",
    ],
    visibility = ["//src:__pkg__"],
)