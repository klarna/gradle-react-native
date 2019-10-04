# Gradle React Native Plugin

[![CircleCI](https://circleci.com/gh/klarna/gradle-react-native.svg?style=svg)](https://circleci.com/gh/klarna/gradle-react-native)
[![codecov](https://codecov.io/gh/klarna/gradle-react-native/branch/master/graph/badge.svg)](https://codecov.io/gh/klarna/gradle-react-native)
[![gradlePluginPortal](https://img.shields.io/maven-metadata/v?label=plugin&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fklarna%2Fgradle%2Freactnative%2Fcom.klarna.gradle.reactnative.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.klarna.gradle.reactnative) ![GitHub](https://img.shields.io/github/license/klarna/gradle-react-native)

- [Gradle React Native Plugin](#gradle-react-native-plugin)
  - [Setup](#setup)
  - [Usage](#usage)
    - [Minimalistic](#minimalistic)
    - [Extended](#extended)
    - [Compatibility](#compatibility)
  - [Contribute](#contribute)
    - [Enable Git Hooks](#enable-git-hooks)
    - [Publishing](#publishing)
      - [Why Not A Standard Approach](#why-not-a-standard-approach)
    - [Run CircleCI locally](#run-circleci-locally)
    - [Snapshots development](#snapshots-development)
  - [References](#references)
  - [Legal](#legal)

## Setup

Gradle's [kotlin-dsl][kotlin_dsl]:

```groovy
// build.gradle.kts
plugins {
    id "com.klarna.gradle.reactnative" version "$version" apply false
}
```

```groovy
// project build.gradle.kts
plugins {
    id("com.android.application")
    id("com.klarna.gradle.reactnative")
}
```

Or via the `buildscript` block:

```groovy
// rootProject build.gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.klarna.gradle.reactnative:plugin:$version"
    }
}
```

```groovy
// project build.gradle
apply plugin: "com.android.application"
apply plugin: "com.klarna.gradle.reactnative"
```

## Usage

### Minimalistic

```groovy
// project build.gradle
apply plugin: "com.android.application"
apply plugin: "com.klarna.gradle.reactnative"
```

After applying plugin all tasks will be created automatically and attached to the build graph.

### Extended

```groovy
// project build.gradle
apply plugin: "com.android.application"
apply plugin: "com.klarna.gradle.reactnative"

/* https://developer.android.com/studio/build/index.html */
android {
    /* ... Configuration of the Android app ... */
}

react {
    /* declare custom configuration options for each build type */
    buildTypes {
        debug {
            /* ... */
        }
        release {
            /* ... */
        }
        /* ... more build types ... */
    }
    
    /* declare custom configuration options for each flavor */
    productFlavors {
        local {
            /* ... */
        }
        /* ... more flavors ... */
    }
    
    /* Reconfigure `android.packagingOptions{...}` for supporting well JSC integration. */
    applyJscPackagingOptions()
}
```

Build types and Flavors are corresponding the Android Build Plugin - [android_build_variants]

### Compatibility

Our plugin is designed for replacing the old React Native `react.gradle` code by properly maintained and heavily tested code. Just for that purpose everything in plugin configuration is backward compatible with old approaches.

We are targeting to replace RN 0.61 [build scripts](https://github.com/facebook/react-native/blob/0.61-stable/react.gradle).

Those lines of code below are equal:

```groovy
project.ext.react = [
    entryFile   : "index.js",
    enableHermes: false,  // clean and rebuild if changing
]

// replaced by

react {
    entryFile = "index.js"
    enableHermes = false
}
``` 

When you use the plugin DSL for configuration, plugin will take care for reflecting 
any defined configuration in `project.ext.react` array/collection. In other words you 
can combine old and approach and current plugin in one project at the same time.

The biggest advantage of the plugin that all the variables definitions are type 
safe and validated in the moment of gradle script running/editing (depends on IDE you use).  


## Contribute

### Enable Git Hooks

In the root folder of the project you can find folder `.githooks` inside it you can find pre-configured hooks.
To enable them just copy all files from `.githooks` to `.git/hooks`.

OR which is a bette approach create a symbolic links to hooks:

```bash
cd .git/hooks
# drop all old hooks
rm *
# create all symbolic links
find ../../.githooks -type f -exec basename {} \; | xargs -I {} ln -s ../../.githooks/{} {}
```

### Publishing

Do login:

```bash
./gradlew login
```

Output:

```text
# Task ':login' is not up-to-date because:
#  Task has not declared any outputs despite executing actions.
#
# To add your publishing API keys to /usr/local/opt/gradle/gradle.properties, open the following URL in your browser:
#
#   https://plugins.gradle.org/api/v1/login/auth/{token}
#
# Your publishing API keys have been written to: $GRADLE_USER_HOME/gradle.properties
```

Copy `gradle.publish.key` and `gradle.publish.secret` keys:

```bash
cat $GRADLE_HOME/gradle.properties
```

```text
#Updated secret and key with server message: Using key 'C02WW29ZHTDD' for OlKu
#Thu, 26 Sep 2019 09:41:39 +0200
gradle.publish.key={secret}
gradle.publish.secret={secret2}
```

Create file in root of the project `credentials.properties` and place those keys into it.

```bash
# increase version MINOR part, switch to RELEASE from any (alpha, beta, rc) and force tag apply
gradle/version-up.sh --minor --release --apply

# take version from properties file
export VER=$(cat version.properties | grep snapshot.version | cut -d'=' -f2)

# switch to release TAG as a branch
git checkout -b $VER $VER

# force full rebuild
./gradlew clean build assembleRelease -Pversion=$VER

# now we can publish the version
./gradlew publishPlugins -Pversion=$VER
```

#### Why Not A Standard Approach

as a developer I work with multiple projects, companies, repositories.
Main rule for me is to keep projects as much as possible isolated from local environment of the laptop.
That is why `credentials.properties` approach used - isolate project from global system settings.

References:

- [publishing overview](https://docs.gradle.org/current/userguide/publishing_overview.html)
- [gradle plugin portal](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/)

### Run CircleCI locally

based on: <https://circleci.com/docs/2.0/local-cli/>

```bash
brew install circleci

# validate config
circleci config validate

# run specific job
circleci local execute --job [dependencies|debug|release|test|lint|deploy] -e VAR1=FOO

## run tests 1/4 - specifically second slice: 2
circleci local execute --job test -e TEST_START=2 -e TEST_TOTAL=4

# cleanup
brew uninstall circleci
```

### Snapshots development

If you want to use the latest development SNAPSHOT version on a regular basis, you will need to also need to add the following to the `buildscript` block to ensure Gradle checks more frequently for updates:

```groovy
buildscript {
  // ...
  /* Since the files in the repository change with each build, we need to recheck for changes */
  configurations.all {
    resolutionStrategy {
      cacheChangingModulesFor 0, 'seconds'
      cacheDynamicVersionsFor 0, 'seconds'
    }
  }
}

// `-SNAPSHOT` suffix automatically recognized as `changing` dependency
dependencies {
    compile group: "groupId", name: "artifactId", version: "1+", changing: true
    compile group: "groupId", name: "artifactId", version: "1.0-SNAPSHOT"
}
```

References:

- [Resolution Strategy](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html)
- [Snapshots configuration](https://stackoverflow.com/questions/42058626/how-to-get-newest-snapshot-of-a-dependency-without-changing-version-in-gradle?rq=1)
- [other plugin](https://github.com/bndtools/bnd/blob/master/biz.aQute.bnd.gradle/README.md#using-the-latest-development-snapshot-build-of-the-bnd-gradle-plugins)

## References

- <https://www.klg71.de/post/kotlin_gradle_plugin/>
- <https://github.com/FRI-DAY/elasticmq-gradle-plugin>

## Legal

This project is available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

Copyright 2013-2019 Klarna AB.

[kotlin_dsl]: https://github.com/gradle/kotlin-dsl
[android_build_variants]: https://developer.android.com/studio/build/build-variants
[plugin]: https://plugins.gradle.org/plugin/com.klarna.gradle.reactnative
