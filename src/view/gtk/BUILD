load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "gtk",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "@usr_share_java//:gtk",
        "//src/view",
    ],
    visibility = ["//visibility:public"],
    resources = glob(["res/*"]),
    resource_strip_prefix = "src/view/gtk/res/",
)