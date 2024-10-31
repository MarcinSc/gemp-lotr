dependencies {
    api(project(":gemp-lotr-server"))

    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.1.0")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.114.Final")
    // https://mvnrepository.com/artifact/com.lmax/disruptor
    implementation("com.lmax:disruptor:3.4.4")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.11.0")
    // https://mvnrepository.com/artifact/org.commonmark/commonmark
    implementation("org.commonmark:commonmark:0.24.0")
    // https://mvnrepository.com/artifact/org.commonmark/commonmark-ext-gfm-strikethrough
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.24.0")
    // https://mvnrepository.com/artifact/org.commonmark/commonmark-ext-autolink
    implementation("org.commonmark:commonmark-ext-autolink:0.24.0")
}
