load("@rules_jvm_external//:defs.bzl", "artifact")

# GTK is in the classpath. Used to run gcal from sources
java_binary(
    name = "gcal-app",
    srcs = [],
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["gcal-jar", "@usr_share_java//:gtk-runtime"]
)

genrule(
   name = "gcal-jar",
   srcs = ["_gcal-jar_deploy.jar"],
   outs = ["gcal-notifier-kotlin-gtk.jar"],
   cmd = "cp $(location _gcal-jar_deploy.jar) $(location gcal-notifier-kotlin-gtk.jar)",
)
java_binary(
    name = "_gcal-jar",
    srcs = [],
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["//src"]
)
