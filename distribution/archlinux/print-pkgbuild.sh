#/usr/bin/env sh

if [ $# -ne 2 ]; then
    echo "Usage: $0 version sha256"
    exit 1
fi

version=$1
sha256=$2

echo "# Maintainer: Nikita Bobko <nikitabobko (at) gmail (dot) com>

pkgname=gcal-notifier-kotlin-gtk
pkgver=${version}
pkgrel=1
pkgdesc='Simple Google Calendar notifier for Linux written in Kotlin using GTK lib'
arch=('x86_64' 'i686')
url='https://github.com/nikitabobko/gcal-notifier-kotlin-gtk'
license=('GPL')
depends=('java-gnome-bin' 'java-runtime=8' 'libnotify' 'librsvg')
source=(\"https://github.com/nikitabobko/gcal-notifier-kotlin-gtk/releases/download/v\${pkgver//_/-}/gcal-notifier-kotlin-gtk-v\${pkgver//_/-}.tar\")
sha256sums=(\"$sha256\")

package() {
    cd \${srcdir}/gcal-notifier-kotlin-gtk-v\${pkgver}
    ./install.sh \$pkgdir/
}"
