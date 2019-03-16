#/usr/bin/env sh
set -e
sudo apt update
sudo apt install libjava-gnome-java openjdk-8-jre
./gradlew test -i
