plugins {
    java
}

group = "xyz.unifycraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.18.0")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
}
