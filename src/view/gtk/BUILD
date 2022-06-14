load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
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

kt_jvm_test(
    name = "IconResourceTest",
    test_class = "bobko.gcalnotifier.view.gtk.IconResourceTest",
    srcs = ["IconResourceTest.kt"],
    deps = [
        ":gtk",
        "//test-util"
    ],
)
