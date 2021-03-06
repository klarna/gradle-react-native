/*
 *
 * Copyright (C) 2013-2019 Klarna AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// that a normal thing for unit tests.
@file:Suppress("ComplexMethod", "LongMethod")

package com.klarna.gradle.reactnative

import com.android.build.gradle.AppExtension
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import com.klarna.gradle.reactnative.ReactNativeExtension as RNConfig
import com.klarna.gradle.reactnative.ReactNativeExtension.Companion.EXTENSION as EXTENSION_REACT

/** Unit tests.
 *
 * @see <a href="http://bit.ly/32ZM79Z">Tests</a>
 * @see <a href="http://bit.ly/30HnZY5">AirBNB okReplay</a>
 * */
class GradleReactNativePluginTest {
    @Test
    fun `plugin registers task compileRnBundle`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
        project.plugins.apply(GradleReactNativePlugin.PLUGIN)

        // Then
        assertNotNull(project.tasks.findByName(CompileRnBundleTask.NAME))
    }

    @Test
    fun `plugin register task copyJsBundle`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
        project.plugins.apply(GradleReactNativePlugin.PLUGIN)

        // Then
        assertNotNull(project.tasks.findByName(CopyRnBundleTask.NAME))
    }

    @Test
    fun `plugin register extension`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
        project.plugins.apply(GradleReactNativePlugin.PLUGIN)

        // Then
        val actual: RNConfig? = project.extensions
            .findByName(EXTENSION_REACT) as RNConfig?

        assertNotNull(actual)
        assertEquals("../../", actual.root)
        assertEquals("index.android.bundle", actual.bundleAssetName)
        assertEquals("index.android.js", actual.entryFile)
        assertEquals("ram-bundle", actual.bundleCommand)
    }

    @Test
    fun `plugin nested extensions, buildTypes`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
        project.plugins.apply(GradleReactNativePlugin.PLUGIN)

        val ext = project.extensions.getByName(EXTENSION_REACT)
            as? RNConfig

        assertNotNull(ext)
        assertNotNull(ext.buildTypes)
//        assertEquals(2, ext.buildTypes.size)

        // composed build types that correspond android project
        ext.buildTypes.forEach {
            assertNotNull(it.name)
            println(it.name)
        }
    }

    @Test(expected = ProjectConfigurationException::class)
    fun `buildTypes configurations are not in sync`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // release, debug, staging
                buildTypes.apply {
                    create("staging")
                }
            }

            configure<RNConfig> {
                buildTypes.apply {
                    create("debug").apply {
                        bundleIn = false
                    }
                    create("unknown").apply {
                        bundleIn = true
                        enableHermes = false
                    }
                }
            }
        }

        // then
        (project as ProjectInternal).evaluate()

        // when
        /*
        android {
            ...
            buildTypes {
                debug { ... }
                release { ... }
                staging { ... }
            }
        }
        react {
            buildTypes {
                unknown {} // <- this one is wrong
                debug { ... }
            }
        }
        */
    }

    @Test(expected = ProjectConfigurationException::class)
    fun `productFlavors configurations are not in sync`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // local, yellow, release
                flavorDimensions("dummy")
                productFlavors.apply {
                    create("local").apply {}
                    create("yellow").apply {}
                    create("pink").apply {}
                }
            }

            configure<RNConfig> {
                productFlavors.apply {
                    create("legacy").apply {
                        bundleIn = true
                        enableHermes = true
                    }
                    create("pink").apply {
                        enableHermes = true
                    }
                }
            }
        }

        // force evaluation of the gradle project
        (project as ProjectInternal).evaluate()

        /*
        android {
            ...
            productFlavors {
                local { ... }
                yellow { ... }
                pink { ... }
            }
        }
        react {
            productFlavors {
                legacy { ... }   // <- this one is wrong
                pink { ... }
            }
        }
        * */
    }

    @Test
    fun `flavors matrix full configuration`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // release, debug, staging
                buildTypes.apply {
                    create("staging")
                }

                // local, yellow, release
                flavorDimensions("dummy")
                productFlavors.apply {
                    create("local").apply {}
                    create("yellow").apply {}
                    create("pink").apply {}
                }
            }

            configure<RNConfig> {
                buildTypes.apply {
                    create("debug").apply {
                        bundleIn = false
                    }
                }
                productFlavors.apply {
                    create("pink").apply {
                        enableHermes = true
                    }
                }
            }
        }

        // force evaluation of the gradle project
        (project as ProjectInternal).evaluate()

        val react = project.extensions.getByName(EXTENSION_REACT) as RNConfig
        assertNotNull(react)

        with(react) {
            assertEquals(3, buildTypes.size, "expected: release, debug, staging")
            assertEquals(3, productFlavors.size, "expected: local, yellow, pink")

            assertNotNull(buildTypes.findByName("debug"))
            assertNotNull(buildTypes.findByName("release"))
            assertNotNull(buildTypes.findByName("staging"))

            assertNotNull(productFlavors.findByName("local"))
            assertNotNull(productFlavors.findByName("yellow"))
            assertNotNull(productFlavors.findByName("pink"))
        }
    }

    @Test
    fun `apply applyJscPackagingOptions() for android application`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                packagingOptions.apply {
                    exclude("META-INF/LICENSE")
                    pickFirst("**/libjsc.so")
                }
            }

            configure<RNConfig> {
                applyJscPackagingOptions()
            }
        }

        // force evaluation of the gradle project
        (project as ProjectInternal).evaluate()

        val android = GradleReactNativePlugin.getAndroidConfiguration(project)
        with(android.packagingOptions.pickFirsts) {
            assertTrue(any { "**/x86/libjsc.so" == it })
            assertTrue(any { "**/armeabi-v7a/libjsc.so" == it })
            assertTrue(any { "**/x86/libc++_shared.so" == it })
            assertTrue(any { "**/x86_64/libc++_shared.so" == it })
            assertTrue(any { "**/armeabi-v7a/libc++_shared.so" == it })
            assertTrue(any { "**/arm64-v8a/libc++_shared.so" == it })
            assertTrue(any { "**/libjsc.so" == it })
        }
    }

    @Test
    fun `project extra properties not published in OFF compatibility mode`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // release, debug, staging
                buildTypes.apply {
                    create("staging")
                }

                // local, yellow, release
                flavorDimensions("dummy")
                productFlavors.apply {
                    create("local").apply {}
                    create("yellow").apply {}
                    create("pink").apply {}
                }
            }

            configure<RNConfig> {
                enableCompatibility = false
                buildTypes.apply {
                    create("debug").apply {
                        bundleIn = false
                    }
                }
                productFlavors.apply {
                    create("pink").apply {
                        enableHermes = true
                    }
                }
            }
        }

        // then
        (project as ProjectInternal).evaluate()

        // when
        assertNotNull(project.extensions.getByName(EXTENSION_REACT), "plugin extension exists")
        assertTrue(project.extra.has("react"), "compatibility extras exists")
        @Suppress("UNCHECKED_CAST")
        val rc = project.extra.get("react") as Map<String, *>
        assertNotNull(rc["enableHermes"], "at least enableHermes should be published on high-level")
    }

    @Test
    fun `project extra properties reflect DSL configuration`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // release, debug, staging
                buildTypes.apply {
                    create("staging")
                }

                // local, yellow, release
                flavorDimensions("dummy")
                productFlavors.apply {
                    create("local").apply {}
                    create("yellow").apply {}
                    create("pink").apply {}
                }
            }

            configure<RNConfig> {
                enableCompatibility = true
                buildTypes.apply {
                    create("debug").apply {
                        bundleIn = false
                    }
                }
                productFlavors.apply {
                    create("pink").apply {
                        enableHermes = true
                    }
                }
            }
        }

        // then
        (project as ProjectInternal).evaluate()

        // when
        assertNotNull(project.extensions.getByName(EXTENSION_REACT), "plugin extension exists")
        assertTrue(project.extra.has("react"), "compatibility extras exists")

        @Suppress("UNCHECKED_CAST")
        val rc = project.extra.get("react") as Map<String, *>
        assertNotNull(rc, "compatibility extras exists")

        // verify that value is set
        assertEquals("index.android.js", rc["entryFile"], "entryFile")
        assertNotNull(rc["bundleAssetName"])
        assertNotNull(rc["entryFile"])
        assertNotNull(rc["bundleCommand"])
        assertNotNull(rc["root"])
        assertNotNull(rc["jsBundleDirRelease"])
        assertNotNull(rc["jsBundleDirDebug"])
        assertNotNull(rc["jsBundleDirStaging"])
        assertNotNull(rc["resourcesDirRelease"])
        assertNotNull(rc["resourcesDirDebug"])
        assertNotNull(rc["resourcesDirStaging"])
        assertNotNull(rc["inputExcludes"])
        assertNotNull(rc["nodeExecutableAndArgs"])
        assertNotNull(rc["extraPackagerArgs"])
    }

    @Test
    fun `project extra properties reflect DSL configuration for flavors`() {
        val project = ProjectBuilder.builder().build()

        // given
        with(project) {
            plugins.apply(GradleReactNativePlugin.ANDROID_APP_PLUGIN)
            plugins.apply(GradleReactNativePlugin.PLUGIN)

            configure<AppExtension> {
                compileSdkVersion(29)
                buildToolsVersion("29.0.2")

                defaultConfig.apply {
                    versionCode = 1
                    versionName = "0.1"
                    setMinSdkVersion(19)
                    setTargetSdkVersion(29)
                    applicationId = "dummy.myapplication"
                }

                // release, debug, staging
                buildTypes.apply {
                    create("staging")
                }

                // local, yellow, release
                flavorDimensions("dummy")
                productFlavors.apply {
                    create("local").apply {}
                    create("yellow").apply {}
                    create("pink").apply {}
                }
            }

            configure<RNConfig> {
                enableCompatibility = true
                buildTypes.apply {
                    create("debug").apply {
                        bundleIn = false
                    }
                }
                productFlavors.apply {
                    create("pink").apply {
                        enableHermes = true
                    }
                }
            }
        }

        // then
        (project as ProjectInternal).evaluate()

        // when
        assertNotNull(project.extensions.getByName(EXTENSION_REACT), "plugin extension exists")
        assertTrue(project.extra.has("react"), "compatibility extras exists")

        @Suppress("UNCHECKED_CAST")
        val rc = project.extra.get("react") as Map<String, *>
        assertNotNull(rc, "compatibility extras exists")

        // check that build type flag applied properly
        assertEquals(false, rc["bundleInPinkDebug"], "bundleInPinkDebug")
        assertEquals(false, rc["bundleInYellowDebug"], "bundleInYellowDebug")
        assertEquals(false, rc["bundleInLocalDebug"], "bundleInLocalDebug")

        // check that flavor flag applied properly
        assertEquals(true, rc["enableHermesPinkDebug"], "enableHermesPinkDebug")
        assertEquals(true, rc["enableHermesPinkRelease"], "enableHermesPinkRelease")
        assertEquals(true, rc["enableHermesPinkStaging"], "enableHermesPinkStaging")
    }
//    @Test
//    fun ``(){
//
//    }
}
