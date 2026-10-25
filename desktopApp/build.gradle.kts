import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

compose.desktop {
    application {
        mainClass = "dev.fediim.anysdui.preview.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "anysdui-preview"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<Test>().configureEach {
    // ImageComposeScene draws off screen; no display on CI runners.
    systemProperty("java.awt.headless", "true")
}
