#! /usr/bin/env bash

ant clean
ant release

sudo jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore original-content-software.keystore bin/MyMaze-release-unsigned.apk original_content_software || { echo 'Error! Could not sign jar file.'; exit 1; }
~/android/android-sdk-linux/build-tools/21.1.2/zipalign -v 4 bin/MyMaze-release-unsigned.apk bin/MyMaze.apk
jarsigner -verify -verbose -certs bin/MyMaze.apk --keystore original-content-software.keystore
