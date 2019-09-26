#!/bin/bash

set -x # print the executed lines
set -e # fail if any of commands fail

version-up.sh
export VER=$(cat version.properties | grep snapshot.version | cut -d'=' -f2)

../gradlew clean cleanBuildCache
../gradlew dependencies
../gradlew dependencyUpdates -Drevision=release
../gradlew build assembleDebug -x check -Pversion=${VER}-SNAPSHOT
../gradlew build assembleRelease -x check -Pversion=${VER}
../gradlew jacocoRootReport --rerun-tasks
../gradlew lint
../gradlew ktlintCheck
../gradlew detekt
../gradlew dokka
../gradlew lint