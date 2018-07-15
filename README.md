![icon](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/src/main/resources/icon.png)

# gcal-notifier-kotlin-gtk
Simple Google Calendar notifier for Linux written on Kotlin using GTK lib  
The latest version is 1.0

# Installation
* For ArchLinux users [AUR package](https://aur.archlinux.org/packages/gcal-notifier-kotlin-gtk/) is available
* For other Linux distributions:
  * Ensure that you have [java-gnome lib](http://java-gnome.sourceforge.net/) installed
  * Download the latest release on the [release page](https://github.com/nikitabobko/gcal-notifier-kotlin-gtk/releases)
  * Extract tar archieve by executing:  
  `tar -xvf gcal-notifier-kotlin-gtk.tar`
  * Install program by executing `install.sh` script in extracted folder:  
  `./install.sh`

# Usage
While app is running it shows small icon in your system tray and popups notifications for events which are setted to be reminded in Google Calendar:  
![popup.png](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/.screenshots/popup.png)![notif.png](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/.screenshots/notif.png)

`Hint:` Gnome users may not see that system tray icon as system tray feature was removed in Gnome 3.26. You may want to install
[TopIcons](https://extensions.gnome.org/extension/495/topicons/) or [TopIcons Plus](https://extensions.gnome.org/extension/1031/topicons/)
to return back system tray.

# Requirements
* [java-gnome lib](http://java-gnome.sourceforge.net/)

# Uninstallation
* If you installed it using [AUR package](https://aur.archlinux.org/packages/gcal-notifier-kotlin-gtk/):  
`sudo pacman -Rsn gcal-notifier-kotlin-gtk`
* For other users:  
`sudo /opt/gcal-notifier-kotlin-gtk/uninstall.sh`

# Building project from sources
For building project [Gradle build tool](https://gradle.org/) is used.  
Before building project ensure that you have [java-gnome lib](http://java-gnome.sourceforge.net/) installed.
#### Building jar file
Execute `./gradlew jar` to generate jar file in `build/libs/` directory.
#### Building tar archieve
Execute `make tar` to generate tar archieve in project's root directory.
