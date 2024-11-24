plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-context"))
    api(project(":gempukku:gempukku-object-stream"))
}
