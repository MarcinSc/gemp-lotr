plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-server"))

    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.114.Final")
}
