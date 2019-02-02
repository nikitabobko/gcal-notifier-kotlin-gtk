# Search appname and version in settings.gradle and build.gradle using regex
APPNAME=$(shell cat settings.gradle | grep -Po "(?<=(rootProject.name = ')).+(?=')")
VERSION=$(shell cat build.gradle | grep -Po "(?<=(version ')).+(?=')")

MAIN_DIR=$(APPNAME)-v$(VERSION)

DEBIAN_SUPPORT_DIR=debian-support

JAR_FILE=$(MAIN_DIR)/$(APPNAME).jar
JAR_FILE_GRADLE=build/libs/$(APPNAME)-$(VERSION).jar

TAR_FILE=$(APPNAME)-v$(VERSION).tar

DEB_DIR=$(APPNAME)-v$(VERSION)-deb
DEB_FILE=$(APPNAME)-v$(VERSION).deb

.PHONY: $(JAR_FILE_GRADLE)

all: tar deb

######################
### Debian package ###
######################

deb: $(DEB_FILE)

$(DEB_FILE): $(DEB_DIR)
	dpkg-deb --build $< $@

$(DEB_DIR): install_to_DEB_DIR $(DEB_DIR)/DEBIAN/control 

install_to_DEB_DIR: $(MAIN_DIR)
	mkdir -p $(DEB_DIR)
	$(MAIN_DIR)/install.sh $(DEB_DIR)

$(DEB_DIR)/DEBIAN/control:
	mkdir -p $(DEB_DIR)/DEBIAN && \
	cat $(DEBIAN_SUPPORT_DIR)/control > $@ && \
	echo "Version: ${VERSION}" >> $@ && \
	echo "Installed-Size: $(shell du -s $(DEB_DIR) | cut -f1)" >> $@

#############################
### Universal tar package ###
#############################

tar: $(TAR_FILE)

$(TAR_FILE): $(MAIN_DIR)
	tar -cf $@ $<

###########################################################
### Main dir which collects all sources into one folder ###
###########################################################

$(MAIN_DIR): $(JAR_FILE)
	mkdir -p $@ && \
	cp -r support/. $@ && \
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
	./gradlew clean && rm -rf $(MAIN_DIR) $(DEB_DIR) *.tar *.deb
