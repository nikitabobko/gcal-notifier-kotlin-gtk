#!/usr/bin/env sh
java -cp /usr/share/java/gtk.jar:/opt/gcal-notifier-kotlin-gtk/gcal-notifier-kotlin-gtk.jar \
-Xmx10m -Xss1m bobko.gcalnotifier.MainKt $@
