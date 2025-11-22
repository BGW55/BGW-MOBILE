// build.gradle.kts (Project)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ⬇️ [수정] 2.48 -> 2.51.1로 변경 (라이브러리와 버전 일치시키기)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}