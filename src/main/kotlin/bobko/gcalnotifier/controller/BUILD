load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "controller",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/model",
        "//src/main/kotlin/bobko/gcalnotifier/support",
        artifact("ru.nikitabobko.kotlin.refdelegation:kotlin-ref-delegation"),
    ],
    visibility = ["//visibility:public"],
)