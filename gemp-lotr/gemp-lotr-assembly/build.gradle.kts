plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(project(":gemp-lotr-server"))

    api(project(":gemp-core:gemp-collection"))
    api(project(":gemp-core:gemp-db"))
    api(project(":gemp-core:gemp-game"))
    api(project(":gemp-core:gemp-league"))
    api(project(":gemp-core:gemp-player"))
    api(project(":gemp-core:gemp-tournament"))
    api(project(":gemp-core:gemp-transfer"))

    api(project(":gempukku:gempukku-context"))
    api(project(":gempukku:gempukku-chat-server"))
    api(project(":gempukku:gempukku-netty-server"))
    implementation(kotlin("stdlib-jdk8"))

    // https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}
