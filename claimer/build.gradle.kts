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

    implementation("org.mongodb:mongodb-driver-sync:5.1.3")
    implementation("com.github.Solotory:mongo-jackson-codec:1.0.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    implementation("team.unnamed:inject:2.0.1")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")

    implementation("org.slf4j:slf4j-simple:2.0.16")

    implementation("net.raphimc:MinecraftAuth:4.1.0")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.siansxint.sniper.claimer.ClaimerMain"
        }
    }
}