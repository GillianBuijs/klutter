#!/bin/sh
#stop script on failure
set -e

cd ".."

echo " ____________
< Publishing Klutter modules >
 ------------
        \   ^__^
         \  (oo)\_______
            (__)\       )\/\
                ||----w |
                ||     ||"

./gradlew clean build -p "lib/kore"
./gradlew clean build  -p "lib/compiler"
./gradlew clean build  -p "lib/gradle"
./gradlew publishToMavenLocal -p "lib/kore"
./gradlew publishToMavenLocal -p "lib/compiler"
./gradlew publishToMavenLocal -p "lib/gradle"
