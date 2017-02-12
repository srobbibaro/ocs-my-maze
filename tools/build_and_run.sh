#! /usr/bin/env bash

export JAVA_HOME=/opt/android-studio/jre/

./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
./tools/log.sh
