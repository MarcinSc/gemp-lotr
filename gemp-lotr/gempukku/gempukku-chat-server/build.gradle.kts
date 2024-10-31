plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(project(":gempukku:gempukku-context"))
    api(project(":gempukku:gempukku-netty-server"))

    // https://mvnrepository.com/artifact/org.commonmark/commonmark
    implementation("org.commonmark:commonmark:0.24.0")
    // https://mvnrepository.com/artifact/org.commonmark/commonmark-ext-gfm-strikethrough
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.24.0")
    // https://mvnrepository.com/artifact/org.commonmark/commonmark-ext-autolink
    implementation("org.commonmark:commonmark-ext-autolink:0.24.0")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    testImplementation("org.apache.httpcomponents:httpclient:4.5")
}
