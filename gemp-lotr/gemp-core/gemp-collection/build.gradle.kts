plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gemp-core:gemp-common"))
    api(project(":gemp-core:gemp-db"))
    api(project(":gemp-core:gemp-transfer"))
    api(project(":gempukku:gempukku-server"))

    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.8.0")

    testImplementation(project(":gempukku:gempukku-context"))
    testImplementation(testFixtures(project(":gemp-core:gemp-db")))
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    testImplementation("com.mysql:mysql-connector-j:9.1.0")
}
