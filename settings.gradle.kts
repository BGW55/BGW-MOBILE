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
<<<<<<< HEAD
include(":app:w06")
=======
>>>>>>> f98562e77f37e490e590809f7879cdab36dff37f
=======
include(":app:w05")
include(":app:w05_bubble_game")
include(":app:w06")
include(":app:wop-rhythmgame")
<<<<<<< Updated upstream
include(":app:w05_buble_game:wop-calender")
include(":app:wop-calender")
=======
>>>>>>> 039be1a54aa65ffcb7e956fcbb7cfe9d11bbf689
>>>>>>> Stashed changes
