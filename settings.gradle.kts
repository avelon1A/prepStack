pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TechInterviewPrep"
include(":app")
include(":core")
include(":domain")
include(":ui")
include(":bookmarks")
include(":voiceinterview")
include(":data")
include(":ads")