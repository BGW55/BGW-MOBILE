pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "ADP"
include(":app")
include(":app:w03")
include(":app:w04")
include(":app:w04:w05")
include(":app:w05")
include(":app:w05_buble_game")
<<<<<<< HEAD
include(":app:w06")
=======
>>>>>>> f98562e77f37e490e590809f7879cdab36dff37f
