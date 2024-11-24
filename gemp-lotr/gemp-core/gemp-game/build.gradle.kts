plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-server"))
    api(project(":gemp-core:gemp-deck"))
    api(project(":gemp-core:gemp-server-state"))
}
