// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val agp_version by extra("7.3.1")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        val nav_version = "2.5.2"
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}

plugins {
    id("com.android.application") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}