# Search appname and version in settings.gradle and build.gradle using regex
APPNAME=$(shell cat settings.gradle | grep -Po "(?<=(rootProject.name = ')).+(?=')")
VERSION=$(shell cat build.gradle | grep -Po "(?<=(version ')).+(?=')")

JAR_PATH_GRADLE=build/libs/$(APPNAME)-$(VERSION).jar
TEMP_FOLDER_RELEASE=$(APPNAME)-release
JAR_PATH=$(TEMP_FOLDER_RELEASE)/$(APPNAME).jar

tar: $(TEMP_FOLDER_RELEASE) $(JAR_PATH) 
	tar -cf $(APPNAME).tar $(TEMP_FOLDER_RELEASE) && rm -rf $<

tarForce: clean tar

$(TEMP_FOLDER_RELEASE):
	mkdir -p $@ && cp -r support/. $@ && cp src/main/resources/icon.png $@

$(JAR_PATH): $(JAR_PATH_GRADLE) $(TEMP_FOLDER_RELEASE) 
	cp $< $@

$(JAR_PATH_GRADLE):
	./gradlew jar

clean:
	./gradlew clean && rm -f TEMP_FOLDER_RELEASE
