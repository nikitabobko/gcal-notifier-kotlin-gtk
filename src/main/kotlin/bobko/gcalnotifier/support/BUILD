load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "support",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/util",
        "//src/main/kotlin/bobko/gcalnotifier/model",
        artifact("com.google.oauth-client:google-oauth-client-jetty"),
        artifact("com.google.apis:google-api-services-calendar"),
        artifact("com.google.code.gson:gson"),
    ],
    visibility = ["//visibility:public"],
)