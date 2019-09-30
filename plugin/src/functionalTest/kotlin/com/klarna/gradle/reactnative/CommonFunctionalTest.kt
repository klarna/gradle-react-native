package com.klarna.gradle.reactnative

import kotlin.test.BeforeTest
import org.gradle.testkit.runner.InvalidPluginMetadataException
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.gradle.util.GUtil
import org.junit.Rule
import org.junit.rules.TestName
import java.io.File

open class CommonFunctionalTest {
    protected var projectDir: File
    protected val jacocoDir: File
    protected val metadataDir: File

    @Rule
    @JvmField
    public val testName: TestName = TestName()

    init {
        val pwd = File("").absolutePath
        val projectRootDirName = "/gradle-react-native/plugin"
        if (!pwd.contains(projectRootDirName)) {
            throw AssertionError("Wrong working directory set for functional tests.")
        }

        projectDir = File("build/functionalTest")
        jacocoDir = File("build/jacoco/functionalTest")
        metadataDir = File("build/pluginUnderTestMetadata")
    }

    @BeforeTest
    public open fun initializeDirectory() {
        println("~> ${testName.methodName} <~")

        // make test name safe for using as a file name
        val testName = testName.methodName.replace(
            "[^a-z0-9.-]".toRegex(RegexOption.IGNORE_CASE), "_"
        )

        projectDir = File("build/functionalTest/$testName")
        projectDir.mkdirs()

        // make empty files
        projectDir.resolve("settings.gradle").writeText("$ANDROID_PLUGIN")
        projectDir.resolve("build.gradle").writeText("")

        // runtime jacoco attaching
        val expandedDir = "build/tmp/expandedArchives"
        val jacocoVer = "org.jacoco.agent-0.8.4.jar"
        val jacocoRuntime: File = File("$expandedDir/$jacocoVer/jacocoagent.jar")
        val javaAgent = "-javaagent:${jacocoRuntime.absolutePath}" +
            "=destfile=${jacocoDir.absolutePath}/$testName.exec"
        val memory = "-Xmx64m -Xms64m" +
            " -Dkotlin.daemon.jvm.options=\"-Xmx64m\"" +
            " -Dkotlin.compiler.execution.strategy=\"in-process\""
        projectDir.resolve("gradle.properties").writeText(
            """
            # method=${this.testName.methodName}
            org.gradle.caching=false
            org.gradle.daemon=false
            org.gradle.jvmargs=$javaAgent $memory -Dfile.encoding=UTF-8
            """.trimIndent()
        )
    }

    /** Helper that should solve classpath loading for Unit tests running in IDE. */
    protected fun loadClasspath(): List<File> {
        try {
            return PluginUnderTestMetadataReading.readImplementationClasspath()
        } catch (ignored: InvalidPluginMetadataException) {
            /* we cannot extract `plugin-under-test-metadata.properties` file from resources. */
        }

        with(File(metadataDir, "plugin-under-test-metadata.properties")) {
            if (!exists()) {
                throw AssertionError("./gradlew :plugin:pluginUnderTestMetadata not called.")
            }
            val properties = GUtil.loadProperties(this)

            return PluginUnderTestMetadataReading.readImplementationClasspath(
                absolutePath,
                properties
            )
        }
    }
}
