plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-server"))
    api(project(":gemp-core:gemp-deck"))
}
