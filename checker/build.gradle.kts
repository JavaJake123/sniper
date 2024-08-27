plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.0")
}

group = "me.siansxint.sniper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":common"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

    implementation("org.mongodb:mongodb-driver-sync:5.1.3")
    implementation("com.github.Solotory:mongo-jackson-codec:1.0.0")

    implementation("org.slf4j:slf4j-simple:2.0.16")

    implementation("team.unnamed:inject:2.0.1")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.siansxint.sniper.checker.CheckerMain"
        }
    }
}