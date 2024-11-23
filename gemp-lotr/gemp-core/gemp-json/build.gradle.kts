plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-context"))

    // https://mvnrepository.com/artifact/org.hjson/hjson
    api("org.hjson:hjson:3.1.0")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
}
