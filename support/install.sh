#!/usr/bin/env sh
root=/
if [ "$#" -eq 1 ]; then
    root=$1
fi

install -D -m644 gcal-notifier-kotlin-gtk.jar ${root}/opt/gcal-notifier-kotlin-gtk/gcal-notifier-kotlin-gtk.jar
install -D -m644  icon.png ${root}/opt/gcal-notifier-kotlin-gtk/icon.png
install -D launcher.sh ${root}/usr/bin/gcal-notifier-kotlin-gtk
install -D -m644 desktop-entry.desktop ${root}/usr/share/applications/gcal-notifier-kotlin-gtk.desktop
install -D .uninstall-1.0.sh ${root}/opt/gcal-notifier-kotlin-gtk/.uninstall-1.0.sh
install -D uninstall.sh ${root}/opt/gcal-notifier-kotlin-gtk/uninstall.sh
