load("@rules_pkg//pkg:deb.bzl", "pkg_deb")
load("@rules_pkg//pkg:tar.bzl", "pkg_tar")
load("@rules_pkg//pkg:mappings.bzl", "pkg_files", "pkg_attributes")

pkg_tar(
    name = "_gcal-tar",
    srcs = [
        ":desktop-entry.desktop",
        "uninstall.sh",
        "install.sh",
        "README",
        "launcher.sh",
        "//:gcal-jar",
        "//src/view/gtk:res/gcal-icon.png",
    ],
    visibility = ["//:__pkg__"],
    package_dir = "gcal-notifier-kotlin-gtk", # tar root dir
)

pkg_deb(
    name = "_gcal-deb",
    package = "gcal-notifier-kotlin-gtk",
    description = "Simple Google Calendar notifier for Linux written in Kotlin using GTK lib",
    homepage = "https://github.com/nikitabobko/gcal-notifier-kotlin-gtk",
    data = ":deb_tar",
    depends = ["libjava-gnome-java (>= 4.1.3)", "openjdk-8-jre"],
    maintainer = "Nikita Bobko <gcal-notifier@bobko.xyz>",
    version_file = "//src/util:res/gcal-version.txt",
    visibility = ["//:__pkg__"],
)

pkg_tar(
    name = "deb_tar",
    srcs = [
        ":opt",
        ":opt_exec",
        ":usr_bin",
        ":usr_share_applications",
    ],
)

pkg_files(
    name = "opt",
    srcs = [
        "//:gcal-jar",
        "//src/view/gtk:res/gcal-icon.png",
    ],
    prefix = "opt/gcal-notifier-kotlin-gtk",
)

pkg_files(
    name = "opt_exec",
    srcs = ["uninstall.sh"],
    attributes = pkg_attributes(mode = "0755"),
    prefix = "opt/gcal-notifier-kotlin-gtk",
)

pkg_files(
    name = "usr_bin",
    srcs = ["launcher.sh"],
    prefix = "usr/bin",
    attributes = pkg_attributes(mode = "0755"),
    renames = {"launcher.sh": "gcal-notifier-kotlin-gtk"},
)

pkg_files(
    name = "usr_share_applications",
    srcs = ["desktop-entry.desktop"],
    prefix = "usr/share/applications",
    renames = {"desktop-entry.desktop": "gcal-notifier-kotlin-gtk.desktop"},
)
