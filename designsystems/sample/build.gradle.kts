import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The sample design system, copied from anysdui-kmp's sample-designsystem at anysdui.sourceCommit.
// It is a module of this template and is never published. A company's own design system is built
// the same way: components annotated for the AnySDUI processor, a generated renderer, and a
// catalog.json from the same annotations (see the README).
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

val anysduiVersion = requiredProperty("anysdui.version")

kotlin {
    listOf(iosArm64(), iosSimulatorArm64())

    jvm()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    android {
        namespace = "dev.fediim.anysdui.designsystem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
        androidResources { enable = true }
    }
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
            dependencies {
                implementation("dev.fediim:anysdui-annotations:$anysduiVersion")
                implementation("dev.fediim:anysdui-runtime:$anysduiVersion")
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
            }
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

// Keeps the package the copied sources import (anysdui-kmp derives it from its root project name).
compose.resources { packageOfResClass = "anysdui_kmp.sample_designsystem.generated.resources" }

ksp {
    arg("anysdui.catalogId", "urn:anysdui:sample-designsystem")
    arg("anysdui.libraryVersion", "1.0.0")
    // The catalog `instructions` (RFC-0004 RD-29). The text is the option, so editing the file
    // reruns KSP.
    arg(
        "anysdui.instructions",
        providers
            .fileContents(layout.projectDirectory.file("catalog-instructions.md"))
            .asText
            .get(),
    )
}

dependencies {
    add("kspCommonMainMetadata", "dev.fediim:anysdui-processor:$anysduiVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
