// https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin

// used for the kotlin-dsl
buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }
}

plugins {
  `kotlin-dsl`
}

// For everything else
repositories {
  mavenLocal()
  mavenCentral()
}
