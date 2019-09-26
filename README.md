# Gradle React Native Plugin

[![CircleCI](https://circleci.com/gh/klarna/gradle-react-native.svg?style=svg)](https://circleci.com/gh/klarna/gradle-react-native)
[![codecov](https://codecov.io/gh/klarna/gradle-react-native/branch/master/graph/badge.svg)](https://codecov.io/gh/klarna/gradle-react-native)
[![gradlePluginPortal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/klarna/gradle-react-native/com.klarna.gradle.reactnative.gradle.plugin/maven-metadata.xml.svg?label=gradlePluginPortal)](https://plugins.gradle.org/plugin/com.klarna.gradle.reactnative)


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

```groovy
// project build.gradle
apply plugin: "com.android.application"
apply plugin: "com.klarna.gradle.reactnative"

android {
    /* ... Configuration of the Android app ... */
}

react {
    buildTypes {
        // ...
    }
    productFlavors {
        // ...
    }
}
```

## Contribute

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
./gradlew publishPlugins
```

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

## References:

* <https://www.klg71.de/post/kotlin_gradle_plugin/>
* <https://github.com/FRI-DAY/elasticmq-gradle-plugin>

[kotlin_dsl]: https://github.com/gradle/kotlin-dsl
