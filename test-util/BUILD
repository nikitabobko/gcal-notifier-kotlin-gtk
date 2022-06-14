load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "test-util",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/model",
        "//src/util",
        artifact("org.mockito:mockito-core"),
    ],
    visibility = ["//visibility:public"],
    exports = [
        artifact("org.jetbrains.kotlin:kotlin-test-junit"),
        artifact("org.mockito:mockito-core"),
    ],
    runtime_deps = [
        "@usr_share_java//:gtk-runtime",
    ],
)
