plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gemp-core:gemp-db"))
    api(project(":gemp-core:gemp-game"))
    api(project(":gemp-core:gemp-json"))

    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.8.0")
}
