plugins {
    java
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    // Required to download KtLint
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

subprojects {
    apply {
        plugin<JavaLibraryPlugin>()
        from("$rootDir/dependencies.gradle.kts")
    }
}
