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

android {
    /* ... Configuration of the Android app ... */
}

react {
    buildTypes {
        debug {
            /* ... */
        }
        release {
            /* ... */
        }
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

References:
* [publishing overview](https://docs.gradle.org/current/userguide/publishing_overview.html)
* [gradle plugin portal](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/)

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

* [Resolution Strategy](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html)
* [Snapshots configuration](https://stackoverflow.com/questions/42058626/how-to-get-newest-snapshot-of-a-dependency-without-changing-version-in-gradle?rq=1)
* [other plugin](https://github.com/bndtools/bnd/blob/master/biz.aQute.bnd.gradle/README.md#using-the-latest-development-snapshot-build-of-the-bnd-gradle-plugins)

## References:

* <https://www.klg71.de/post/kotlin_gradle_plugin/>
* <https://github.com/FRI-DAY/elasticmq-gradle-plugin>

[kotlin_dsl]: https://github.com/gradle/kotlin-dsl
