rootProject.name = "anysdui-preview-template"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Where dev.fediim resolves from: "mavenLocal" (default) or "githubPackages". GitHub Packages
// credentials come from the environment (GITHUB_ACTOR, ANYSDUI_PACKAGES_TOKEN) or from the user's
// own gradle.properties (gpr.user, gpr.key); they are never written into this repository.
val anysduiRepository = providers.gradleProperty("anysduiRepository").getOrElse("mavenLocal")

dependencyResolutionManagement {
    repositories {
        // dev.fediim resolves from exactly one repository, so a copy elsewhere can never mask a
        // defect.
        exclusiveContent {
            forRepository {
                when (anysduiRepository) {
                    "mavenLocal" -> mavenLocal()
                    "githubPackages" ->
                        maven {
                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/fediim-dev/anysdui-kmp")
                            credentials {
                                username =
                                    providers
                                        .environmentVariable("GITHUB_ACTOR")
                                        .orElse(providers.gradleProperty("gpr.user"))
                                        .orNull
                                password =
                                    providers
                                        .environmentVariable("ANYSDUI_PACKAGES_TOKEN")
                                        .orElse(providers.gradleProperty("gpr.key"))
                                        .orNull
                            }
                        }
                    else -> error("anysduiRepository must be mavenLocal or githubPackages")
                }
            }
            filter { includeGroup("dev.fediim") }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app")

include(":androidApp")

include(":desktopApp")

include(":webApp")

// The sample design system, a module of this template (never published). A company's own design
// system module is added the same way; see the README.
include(":designsystems:sample")
