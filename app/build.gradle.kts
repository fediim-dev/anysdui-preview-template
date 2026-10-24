import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val preview = previewProperties()
val anysduiVersion = requiredProperty("anysdui.version")

val generatePreviewSources by
    tasks.registering(GeneratePreviewSources::class) {
        serverOrigin = preview.serverOrigin
        preview.maxComponents?.let { maxComponents = it }
        preview.maxPayloadBytes?.let { maxPayloadBytes = it }
        preview.maxFrameBytes?.let { maxFrameBytes = it }
        companyRenderers = preview.renderers
        // The order of the built-ins is their precedence order, basic last (RFC-0004 A.4).
        builtInCatalogs.from(
            listOf("foundation", "material3", "material", "charts", "cupertino", "basic").map {
                layout.projectDirectory.file("catalogs/compose-$it-catalog.json")
            }
        )
        companyCatalogs.from(preview.catalogs.map { rootProject.file(it) })
        outputDir = layout.buildDirectory.dir("generated/preview/commonMain/kotlin")
    }

val generateIosConfig by
    tasks.registering(GenerateIosConfig::class) {
        previewHost = preview.previewHost
        teamId = preview.appleTeamId
        bundleId = preview.applicationId
        outputFile =
            rootProject.layout.projectDirectory.file(
                "iosApp/Configuration/Preview.generated.xcconfig"
            )
    }

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    android {
        namespace = "dev.fediim.anysdui.preview.app"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
        androidResources { enable = true }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatePreviewSources.flatMap { it.outputDir })
            dependencies {
                api("dev.fediim:anysdui-runtime:$anysduiVersion")
                implementation("dev.fediim:anysdui-transport-ktor:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-foundation:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-material3:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-material:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-charts:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-cupertino:$anysduiVersion")
                implementation("dev.fediim:anysdui-compose-basic:$anysduiVersion")
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                // The design system, compiled in (Gradle properties designSystem.*).
                for (coordinate in preview.coordinates) {
                    if (coordinate.startsWith(":")) implementation(project(coordinate))
                    else implementation(coordinate)
                }
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            // The render tests draw off screen with ImageComposeScene, which needs skiko's native
            // library.
            implementation(compose.desktop.currentOs)
        }
    }
}

tasks.named<Test>("jvmTest") {
    // ImageComposeScene draws off screen; no display on CI runners.
    systemProperty("java.awt.headless", "true")
    // NoRuntimeLoadingTest scans every *Main source set; declared so an edit in iosMain, say,
    // re-runs it instead of restoring a cached pass.
    inputs
        .files(fileTree("src") { include("*Main/**/*.kt") })
        .withPropertyName("mainSourceSetsScannedByTests")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs
        .files(generatePreviewSources.map { it.outputDir })
        .withPropertyName("generatedPreviewSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
