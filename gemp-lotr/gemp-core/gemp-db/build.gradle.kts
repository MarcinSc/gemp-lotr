plugins {
    id("buildlogic.kotlin-library-conventions")
    // which produces test fixtures
    `java-test-fixtures`
}

dependencies {
    api(project(":gempukku:gempukku-context"))

    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.8.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-dbcp2
    implementation("org.apache.commons:commons-dbcp2:2.12.0")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    // https://mvnrepository.com/artifact/ch.vorburger.mariaDB4j/mariaDB4j
    testFixturesImplementation("ch.vorburger.mariaDB4j:mariaDB4j:3.1.0")
}
