![icon](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/src/main/resources/icon.png)

# gcal-notifier-kotlin-gtk
Simple Google Calendar notifier for Linux written on Kotlin using GTK lib  

# Installation

### Debian/Ubuntu

* Download *.deb file from [latest release page](https://github.com/nikitabobko/gcal-notifier-kotlin-gtk/releases/latest)
* Double click on downloaded file and press `Install` button **or** via Terminal:  
  `sudo apt install ./gcal-notifier-kotlin-gtk-VERSION.deb`

### Arch Linux

For Arch Linux users [AUR package](https://aur.archlinux.org/packages/gcal-notifier-kotlin-gtk/) is available

### Other Linux distributions
* Install [java-gnome lib](http://java-gnome.sourceforge.net/)
* Download *.tar file from [latest release page](https://github.com/nikitabobko/gcal-notifier-kotlin-gtk/releases/latest)
* Extract tar archive by executing:  
`tar -xvf gcal-notifier-kotlin-gtk-VERSION.tar`
* Install program by executing `install.sh` script in extracted folder:  
`sudo ./install.sh`

# Usage
While app is running it shows small icon in your system tray and popups notifications for events which are set to be reminded in Google Calendar:  
![popup.png](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/.screenshots/popup.png)![notif.png](https://raw.githubusercontent.com/nikitabobko/gcal-notifier-kotlin-gtk/master/.screenshots/notif.png)

`Hint:` Gnome users may not see that system tray icon as system tray feature was removed in Gnome 3.26. You may want to install
[TopIcons](https://extensions.gnome.org/extension/495/topicons/) or [TopIcons Plus](https://extensions.gnome.org/extension/1031/topicons/)
to return back system tray.

# Requirements
* [java-gnome lib](http://java-gnome.sourceforge.net/)

# Uninstallation

For all uninstallation methods it's recommended to log out from gcal-notifier firstly. You can do this by:  
 `Click gcal-notifier icon on system tray` -> `Log out`

### Debian/Ubuntu

Execute from terminal:  
`sudo apt autoremove gcal-notifier-kotlin-gtk`

### Arch Linux

Execute from terminal:  
`sudo pacman -Rsn gcal-notifier-kotlin-gtk`

### Other Linux distributions

Execute from terminal:  
`sudo /opt/gcal-notifier-kotlin-gtk/uninstall.sh`

# Building project from sources
For building project [Gradle build tool](https://gradle.org/) is used.  
Before building project ensure that you have [java-gnome lib](http://java-gnome.sourceforge.net/) installed.

#### Building jar file
Execute `./gradlew jar` to generate jar file in `build/libs/` directory.

#### Run debug gcal-notifier version from sources
`./gradlew runJar`

#### Build both tar and deb archives
`make build`

#### Build tar archive only
`make tar`

#### Build deb archive only
`make deb`
