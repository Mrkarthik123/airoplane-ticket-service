#!/bin/bash
# Build SkyJet debug APK.
# One-time setup (macOS):
#   brew install --cask android-commandlinetools
#   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
#   brew install openjdk@21            (Capacitor 7.4 needs Java 21)
#   curl -L -o /tmp/gradle.zip https://services.gradle.org/distributions/gradle-8.11.1-bin.zip
#   unzip -q /tmp/gradle.zip -d $HOME  (gives ~/gradle-8.11.1)
set -e
cd "$(dirname "$0")"
if [ -d /opt/homebrew/opt/openjdk@21 ]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
fi
if [ -z "$ANDROID_HOME" ]; then
  if [ -d "$HOME/Library/Android/sdk" ]; then export ANDROID_HOME="$HOME/Library/Android/sdk";
  elif [ -d /opt/homebrew/share/android-commandlinetools ]; then export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools; fi
fi
echo "sdk.dir=$ANDROID_HOME" > android/local.properties
npm run sync
if [ -x "$HOME/gradle-8.11.1/bin/gradle" ]; then
  (cd android && "$HOME/gradle-8.11.1/bin/gradle" assembleDebug)
else
  (cd android && ./gradlew assembleDebug)
fi
APK="$PWD/android/app/build/outputs/apk/debug/app-debug.apk"
cp "$APK" ../SkyJet.apk 2>/dev/null || cp "$APK" ./SkyJet.apk
echo "APK built: $APK (also copied to SkyJet.apk)"
ls -la "$APK"
