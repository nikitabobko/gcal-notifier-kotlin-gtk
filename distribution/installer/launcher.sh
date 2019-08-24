#!/usr/bin/env sh
java -cp /usr/share/java/gtk.jar:/opt/gcal-notifier-kotlin-gtk/gcal-notifier-kotlin-gtk.jar \
-Xmx8m -Xms8m -Xss228k ru.nikitabobko.gcalnotifier.MainKt $@
