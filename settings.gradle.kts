pluginManagement {
    repositories {
        //maven { url = uri("../maven") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        maven(url = "https://chaquo.com/maven")
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://chaquo.com/maven")
        google()
        gradlePluginPortal()
        mavenCentral()

    }

}

rootProject.name = "Location_test_3"
include(":app")
