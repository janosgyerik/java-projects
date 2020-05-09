plugins {
    java
    application
}

dependencies {
    implementation(project(":common"))
    implementation("org.slf4j:slf4j-simple")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClassName = "tftp.client.TftpClientCli"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
