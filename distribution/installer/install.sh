#!/usr/bin/env sh
root=/
if [ "$#" -eq 1 ]; then
    root=$1
fi

cur=$(dirname $0)

install -D -m644 ${cur}/gcal-notifier-kotlin-gtk.jar ${root}/opt/gcal-notifier-kotlin-gtk/gcal-notifier-kotlin-gtk.jar
install -D -m644  ${cur}/icon.png ${root}/opt/gcal-notifier-kotlin-gtk/icon.png
install -D ${cur}/launcher.sh ${root}/usr/bin/gcal-notifier-kotlin-gtk
install -D -m644 ${cur}/desktop-entry.desktop ${root}/usr/share/applications/gcal-notifier-kotlin-gtk.desktop
install -D ${cur}/uninstall.sh ${root}/opt/gcal-notifier-kotlin-gtk/uninstall.sh
