load("@rules_jvm_external//:defs.bzl", "artifact")

# GTK is in the classpath. Used to run gcal from sources
java_binary(
    name = "gcal-app",
    srcs = [],
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["//src/main/kotlin/bobko/gcalnotifier", "@usr_share_java//:gtk-runtime"]
)

# GTK isn't in the classpath. Used to create release artifacts
java_binary(
    name = "gcal-jar",
    srcs = [],
    create_executable = False,
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["//src/main/kotlin/bobko/gcalnotifier"]
)
