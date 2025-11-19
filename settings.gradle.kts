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
include(":app:w05")
include(":app:w05_bubble_game")
include(":app:w06")
include(":app:wop-rhythmgame")
include(":app:w05_buble_game:wop-calender")
include(":app:wop-calender")
include(":app:wop-calender-p")
