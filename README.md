myMAZE
======

myMAZE is an Android game designed for users to create and share personalized mazes with friends.

## Getting Started

Follow Android development setup instructions here: http://developer.android.com/sdk/installing/index.html?pkg=tools

### Configuration

We do not store secret keys in the source code. They should
be specified in `.../res/values/configuration.xml`. The application will not run
properly without these values set. Here's an example:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="bugsnag_api_key">mybugsnagapikey</string>
</resources>
```

This file is specified in the `.gitignore` file and should not be submitted to
the remote repository.

### Debug Build

To compile debug version: `$ ./gradlew assembleDebug`

To deploy to Android device: `$ ./gradlew installDebug`

Optionally AndroidStudio can be used for these operations.

For information, see: https://developer.android.com/studio/run/index.html

### Release Build

In order to properly build a release, you will need the appropriate keystore file
(not in source control), and the password.

Run: `$ ./gradlew assembleRelease`

Optionally AndroidStudio can be used for this operation.

### Logging/Debug

With a device USB-connected, run: `$ tools/log.sh` for log messages specifically
for this application.
