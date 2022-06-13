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
    name = "ApplicationIdentificationTest",
    srcs = ["ApplicationIdentificationTest.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "ControllerTest",
    srcs = ["ControllerTest.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "EventReminderTrackerTest",
    srcs = ["EventReminderTrackerTest.kt"],
    deps = [":test-utils"],
)

kt_jvm_test(
    name = "MyEventTest",
    srcs = ["MyEventTest.kt"],
    deps = [":test-utils"],
)
