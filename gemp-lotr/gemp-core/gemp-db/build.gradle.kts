plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-context"))

    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.8.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-dbcp2
    implementation("org.apache.commons:commons-dbcp2:2.12.0")
}
