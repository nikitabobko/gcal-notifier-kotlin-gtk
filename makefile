# Search appname in settings.gradle using regex
APPNAME=$(shell cat settings.gradle | grep -Po "(?<=(rootProject.name = ')).+(?=')")
VERSION=$(shell cat src/main/resources/version.txt)

MAIN_DIR=$(APPNAME)-v$(VERSION)

DEBIAN_SUPPORT_DIR=debian-support

JAR_FILE=$(MAIN_DIR)/$(APPNAME).jar
JAR_FILE_GRADLE=build/libs/$(APPNAME)-$(VERSION).jar

TAR_FILE=$(APPNAME)-v$(VERSION).tar

DEB_DIR=$(APPNAME)-v$(VERSION)-deb
DEB_FILE=$(APPNAME)-v$(VERSION).deb

PKGBUILD_FILE=archlinux/PKGBUILD
SRCINFO_FILE=archlinux/.SRCINFO

.PHONY: $(JAR_FILE_GRADLE)

###############
### Targets ###
###############

build: tar deb

release: tar deb pkgbuild

pkgbuild: $(PKGBUILD_FILE) $(SRCINFO_FILE)

deb: $(DEB_FILE)

tar: $(TAR_FILE)

#######################
### Update PKGBUILD ###
#######################

$(SRCINFO_FILE): $(PKGBUILD_FILE)
	(cd archlinux && makepkg --printsrcinfo) > $@

$(PKGBUILD_FILE): $(TAR_FILE)
	./archlinux-support/print-pkgbuild.sh $(VERSION) $$(sha256sum $< | cut -d" " -f1) > $@

######################
### Debian package ###
######################

$(DEB_FILE): $(DEB_DIR)
	dpkg-deb --build $< $@

$(DEB_DIR): $(MAIN_DIR)
	mkdir -p $(DEB_DIR)
	$(MAIN_DIR)/install.sh $(DEB_DIR)
	mkdir -p $(DEB_DIR)/DEBIAN
	cat $(DEBIAN_SUPPORT_DIR)/control > $@/DEBIAN/control
	echo "Version: ${VERSION}" >> $@/DEBIAN/control
	echo "Installed-Size: $$(du -s $(DEB_DIR) | cut -f1)" >> $@/DEBIAN/control

#############################
### Universal tar package ###
#############################

$(TAR_FILE): $(MAIN_DIR)
	tar -cf $@ $<

###########################################################
### Main dir which collects all sources into one folder ###
###########################################################

$(MAIN_DIR): $(JAR_FILE)
	mkdir -p $@
	cp -r support/. $@
	cp src/main/resources/icon.png $@

#############################
### Kotlin compiled files ###
#############################

$(JAR_FILE): $(JAR_FILE_GRADLE)
	install -D $< $@

$(JAR_FILE_GRADLE):
	./gradlew jar

#############
### clean ###
#############

clean:
	./gradlew clean && rm -rf $(MAIN_DIR) $(DEB_DIR) $(APPNAME)* *.tar *.deb
