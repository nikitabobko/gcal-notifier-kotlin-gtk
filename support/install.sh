#!/usr/bin/env sh
mkdir -p /opt/gcal-notifier-kotlin-gtk
cp gcal-notifier-kotlin-gtk.jar /opt/gcal-notifier-kotlin-gtk/gcal-notifier-kotlin-gtk.jar
cp icon.png /opt/gcal-notifier-kotlin-gtk/icon.png
cp launcher.sh /usr/bin/gcal-notifier-kotlin-gtk
cp desktop-entry.desktop /usr/share/applications/gcal-notifier-kotlin-gtk.desktop
