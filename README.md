myMAZE
======

myMAZE is an Android game developed by Original Content Software, starting in fall 2012.

## Getting Started

Follow Android development setup instructions here: http://developer.android.com/sdk/installing/index.html?pkg=tools

To compile debug version: `$ ant debug`

To deploy to Android device: `$ adb install -r bin/MyMaze-debug.apk`

### Release Build

In order to properly build a release, you will need the appropriate keystore file
(not in source control), and the password.

Run: `$ tools/build_release.sh`

### Logging/Debug

With a device USB-connected, run: `$ tools/log.sh` for log messages specifically
for this application.
