load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "support",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        "//src/util",
        "//src/model",
        artifact("com.google.oauth-client:google-oauth-client-jetty"),
        artifact("com.google.apis:google-api-services-calendar"),
        artifact("com.google.code.gson:gson"),
    ],
    visibility = ["//visibility:public"],
    resources = glob(["res/*"]),
    resource_strip_prefix = "src/support/res/",
)

kt_jvm_test(
    name = "ClientSecretResourceTest",
    test_class = "bobko.gcalnotifier.support.ClientSecretResourceTest",
    srcs = ["ClientSecretResourceTest.kt"],
    deps = [
        ":support",
        "//test-util",
    ],
)
