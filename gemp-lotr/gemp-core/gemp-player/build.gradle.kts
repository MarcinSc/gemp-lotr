plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gemp-core:gemp-db"))
    api(project(":gempukku:gempukku-server"))

    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.8.0")
}
