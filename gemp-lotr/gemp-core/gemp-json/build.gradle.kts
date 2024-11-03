plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-context"))

    // https://mvnrepository.com/artifact/org.hjson/hjson
    api("org.hjson:hjson:3.1.0")
}
