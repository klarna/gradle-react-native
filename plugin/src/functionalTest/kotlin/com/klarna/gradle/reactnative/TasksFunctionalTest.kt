package com.klarna.gradle.reactnative

import org.gradle.testkit.runner.GradleRunner
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.test.BeforeTest

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
