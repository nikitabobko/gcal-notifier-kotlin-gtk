load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "util",
    srcs = glob(["*.kt"], exclude = ["*Test.kt"]),
    deps = [
        artifact("com.google.oauth-client:google-oauth-client-jetty"),
        artifact("com.google.apis:google-api-services-calendar"),
        artifact("com.google.code.gson:gson"),
    ],
    resources = glob(["res/*"]),
    visibility = ["//visibility:public"],
)

kt_jvm_test(
    name = "ApplicationVersionTest",
    srcs = ["ApplicationVersionTest.kt"],
    deps = [
        ":util",
        "//src/main/kotlin/bobko/gcalnotifier/test"
    ],
)
