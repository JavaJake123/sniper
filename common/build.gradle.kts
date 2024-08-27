plugins {
    id("java")
}

group = "me.siansxint.sniper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.mongodb:mongodb-driver-sync:5.1.3")

    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

    compileOnly("com.github.Solotory:mongo-jackson-codec:1.0.0")

    compileOnly("team.unnamed:inject:2.0.1")

    compileOnly("org.apache.httpcomponents.client5:httpclient5:5.3.1")
}