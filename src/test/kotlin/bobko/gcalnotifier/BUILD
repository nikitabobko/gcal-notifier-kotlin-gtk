load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test", "kt_jvm_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "test-utils",
    srcs = ["Utils.kt"],
    deps = [
        "//src/main",
        artifact("org.jetbrains.kotlin:kotlin-test-junit"),
        artifact("org.mockito:mockito-core"),
    ],
    runtime_deps = ["@usr_share_java//:gtk-runtime"],
)

kt_jvm_test(
    name = "ApplicationIdentificationTests",
    srcs = ["ApplicationIdentificationTests.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "ControllerTests",
    srcs = ["ControllerTests.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "EventReminderTrackerTests",
    srcs = ["EventReminderTrackerTests.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "MyEventTests",
    srcs = ["MyEventTests.kt"],
    deps = [":test-utils"],
)
