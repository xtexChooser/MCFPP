import kotlin.io.path.Path

plugins {
    kotlin("jvm") version "1.9.23"

    id("top.mcfpp.gradle") version "1.0-SNAPSHOT"

}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("top.mcfpp:mcfpp:1.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

mcfpp {
    version = "1.21"
    description = "qwq"
    targetPath = Path("./build/datapack")
}