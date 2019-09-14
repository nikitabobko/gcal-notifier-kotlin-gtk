#!/bin/bash
set -e
set -v

echo_green() {
    echo -e "\e[1;32m$@\e[0m"
}

input() {
    echo_green $@ >&2
    read it
    echo $it
}

######################
### Update version ###
######################
version=$(input "> Enter release version and press ENTER (current version is $(cat src/main/resources/version.txt)):")
if [ -z $version ]; then
    version=$(cat src/main/resources/version.txt)
fi
echo $version > src/main/resources/version.txt

#####################
### Build release ###
#####################
./gradlew clean
./gradlew allDistributionArchives

##############################
### Prepare release commit ###
##############################
if [ -z $EDITOR ]; then
    EDITOR=vim
fi
changelogFile=$(mktemp)
echo "v$version

Changelog:
* Describe changelog here
* And then save file" > $changelogFile
$EDITOR $changelogFile
git add src/main/resources/version.txt
git commit --file=$changelogFile
git tag v$version

###########################
### Push release commit ###
###########################
git show
input "> Check that you are satisfied with created commit and tag and then press ENTER to push"
git push origin v$version
git push origin master

##############################
### Push release to github ###
##############################
while [ ! -f secrets/github_access_token.txt ]; do
    input "> Please setup github access token at: https://github.com/settings/tokens \n
    > And then put token into secrets/github_access_token.txt \n
    > Then press ENTER"
done
./gradlew githubRelease

######################
### Release to AUR ###
######################
./gradlew pkgbuild
cd build
git clone ssh://aur@aur.archlinux.org/gcal-notifier-kotlin-gtk.git aur
cd aur
cp ../archlinux/PKGBUILD .
makepkg # Check that package is building before publishing to AUR
makepkg --printsrcinfo > .SRCINFO
git add .SRCINFO PKGBUILD
git commit --file=$changelogFile
git show
input "> Check that you are satisfied with created commit and tag and then press ENTER to push to AUR"
git push origin master

#################
### Congrats! ###
#################

echo_green "Congrats! New version (v$version) released!"
