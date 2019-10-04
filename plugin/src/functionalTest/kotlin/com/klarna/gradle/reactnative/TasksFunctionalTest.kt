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

package com.klarna.gradle.reactnative

import org.gradle.testkit.runner.GradleRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TasksFunctionalTest() : CommonFunctionalTest() {

    @BeforeTest
    override fun initializeDirectory() {
        super.initializeDirectory()
    }

    @Test
    fun `can run compile task`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            android {
                $ANDROID_SECTION
            }
            """.trimIndent()
        )

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(loadClasspath())
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }

    @Test
    fun `can run copy task`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            android {
                $ANDROID_SECTION
            }
            """.trimIndent()
        )

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(loadClasspath())
            .withProjectDir(projectDir)
            .withArguments(CopyRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CopyRnBundleTask.DUMMY))
    }
}
