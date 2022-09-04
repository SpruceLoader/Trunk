plugins {
    java
}

group = "xyz.unifycraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation("net.minecraft:launchwrapper:1.12")
}
