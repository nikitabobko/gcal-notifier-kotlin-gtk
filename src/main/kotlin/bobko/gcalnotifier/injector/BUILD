load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "injector",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/controller",
        "//src/main/kotlin/bobko/gcalnotifier/controller/impl",
        "//src/main/kotlin/bobko/gcalnotifier/injected",
        "//src/main/kotlin/bobko/gcalnotifier/util",
        "//src/main/kotlin/bobko/gcalnotifier/view",
        "//src/main/kotlin/bobko/gcalnotifier/view/gtk",
    ],
    visibility = ["//src/main/kotlin/bobko/gcalnotifier:__pkg__"],
)