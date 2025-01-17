import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.changelog)
}

version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))


        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        group = providers.gradleProperty("pluginGroup")
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
//            untilBuild = providers.gradleProperty("pluginUntilBuild")
            untilBuild = provider { null } // unset for the latest IDE version, see: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-p   lugin-extension.html#intellijPlatform-pluginConfiguration-ideaVersion-untilBuild
        }

    }

    pluginVerification {
        ides {
            recommended()
        }
    }

}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    path.set(file("CHANGELOG.md").canonicalPath)
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    repositoryUrl.set(providers.gradleProperty("pluginRepositoryUrl"))
}

tasks {
    test {
        useJUnit()
    }

    buildPlugin {
        dependsOn(patchChangelog)
    }

    publishPlugin {
        // use the version from the first build in .github/workflows/build.yml
        val tmpArchiveFile = project.layout.buildDirectory.file("${project.name}-${project.version}.zip")
        val tmpChannel = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }

        doFirst {
            println ("archive-file       : ${tmpArchiveFile.get()}")
            println ("archive-file-exists: ${tmpArchiveFile.get().asFile.exists()}")
            println ("channel            : $tmpChannel")
        }

        // set by .github/workflows/build.yml
        token = providers.systemProperty("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = tmpChannel

        archiveFile = tmpArchiveFile
    }
}