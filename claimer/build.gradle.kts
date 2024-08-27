plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.0")
}

group = "me.siansxint.sniper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.siansxint.sniper.claimer.ClaimerMain"
        }
    }
}