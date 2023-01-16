/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.regressionTests

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.util.*
import org.junit.Test

@Suppress("DEPRECATION") /* Configurations are scheduled for removal */
class KT55929MetadataConfigurationsTest {

    @Test
    fun `test - deprecated metadata configurations - contain dependencies from common source set`() {
        val project = buildProject {
            enableDefaultStdlibDependency(false)
            enableIntransitiveMetadataConfiguration(true)
            applyMultiplatformPlugin()
        }

        val kotlin = project.multiplatformExtension

        kotlin.jvm()
        kotlin.linuxX64()
        kotlin.linuxArm64()

        kotlin.targetHierarchy.default {
            common {
                group("jvmAndLinux") {
                    addCompilations { it.platformType == KotlinPlatformType.jvm }
                    anyLinux()
                    group("linux")
                }
            }
        }

        val commonMain = kotlin.sourceSets.getByName("commonMain")
        val jvmAndLinuxMain = kotlin.sourceSets.getByName("jvmAndLinuxMain")
        val linuxMain = kotlin.sourceSets.getByName("linuxMain")

        commonMain.dependencies {
            api("org.sample:commonMainApi:1.0.0")
            implementation("org.sample:commonMainImplementation:1.0.0")
        }

        jvmAndLinuxMain.dependencies {
            implementation("org.sample:jvmAndLinuxMain:1.0.0")
        }

        linuxMain.dependencies {
            implementation("org.sample:linuxMain:1.0.0")
        }

        project.evaluate()

        /* Check linuxMain */
        listOf(
            linuxMain.apiMetadataConfigurationName,
            linuxMain.implementationMetadataConfigurationName,
            linuxMain.compileOnlyMetadataConfigurationName
        ).forEach { metadataConfigurationName ->
            project.assertContainsDependencies(
                metadataConfigurationName,
                "org.sample:commonMainApi:1.0.0",
                "org.sample:commonMainImplementation:1.0.0",
                "org.sample:jvmAndLinuxMain:1.0.0",
                "org.sample:linuxMain:1.0.0",
            )
        }

        /* Check jvmAndLinuxMain */
        listOf(
            jvmAndLinuxMain.apiMetadataConfigurationName,
            jvmAndLinuxMain.implementationMetadataConfigurationName,
            jvmAndLinuxMain.compileOnlyMetadataConfigurationName
        ).forEach { metadataConfigurationName ->
            project.assertContainsDependencies(
                metadataConfigurationName,
                "org.sample:commonMainApi:1.0.0",
                "org.sample:commonMainImplementation:1.0.0",
                "org.sample:jvmAndLinuxMain:1.0.0",
            )
        }

        /* Check commonMain */
        listOf(
            jvmAndLinuxMain.apiMetadataConfigurationName,
            jvmAndLinuxMain.implementationMetadataConfigurationName,
            jvmAndLinuxMain.compileOnlyMetadataConfigurationName
        ).forEach { metadataConfigurationName ->
            project.assertContainsDependencies(
                metadataConfigurationName,
                "org.sample:commonMainApi:1.0.0",
                "org.sample:commonMainImplementation:1.0.0",
            )
        }

        /* Check intransitive configurations */
        kotlin.sourceSets.forEach { sourceSet ->
            sourceSet as DefaultKotlinSourceSet
            project.assertNotContainsDependencies(
                sourceSet.intransitiveMetadataConfigurationName,
                "org.sample:commonMainApi:1.0.0"
            )
        }
    }
}
