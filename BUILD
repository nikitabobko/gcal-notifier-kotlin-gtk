load("@rules_jvm_external//:defs.bzl", "artifact")

java_binary(
    name = "gcal-app",
    srcs = [],
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["gcal-jar", "@usr_share_java//:gtk-runtime"]
)

genrule(
   name = "gcal-jar",
   srcs = [":_gcal-jar_deploy.jar"],
   outs = ["gcal-notifier-kotlin-gtk.jar"],
   cmd = "cp $(location _gcal-jar_deploy.jar) $(location gcal-notifier-kotlin-gtk.jar)",
   visibility = ["//distribution:__pkg__"],
)
java_binary(
    name = "_gcal-jar",
    srcs = [],
    main_class = "bobko.gcalnotifier.MainKt",
    runtime_deps = ["//src:main"]
)

genrule(
    name = "gcal-deb",
    srcs = ["//distribution:_gcal-deb"],
    outs = ["gcal-notifier-kotlin-gtk.deb"],
    cmd = "cp $(location //distribution:_gcal-deb) $(location gcal-notifier-kotlin-gtk.deb)",
)

genrule(
    name = "gcal-tar",
    srcs = ["//distribution:_gcal-tar"],
    outs = ["gcal-notifier-kotlin-gtk.tar"],
    cmd = "cp $(location //distribution:_gcal-tar) $(location gcal-notifier-kotlin-gtk.tar)",
)
