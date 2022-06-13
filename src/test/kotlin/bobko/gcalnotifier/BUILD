load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

kt_jvm_library(
    name = "test-utils",
    srcs = ["Utils.kt"],
    runtime_deps = ["@usr_share_java//:gtk-runtime"],
    deps = [
        "//src/main/kotlin/bobko/gcalnotifier/model",
        "//src/main/kotlin/bobko/gcalnotifier/support",
        "//src/main/kotlin/bobko/gcalnotifier/util",
        "//src/main/kotlin/bobko/gcalnotifier/controller",
        "//src/main/kotlin/bobko/gcalnotifier/controller/impl",
        "//src/main/kotlin/bobko/gcalnotifier/view",
        artifact("org.jetbrains.kotlin:kotlin-test-junit"),
        artifact("org.mockito:mockito-core"),
    ],
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
