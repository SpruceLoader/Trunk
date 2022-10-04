plugins {
    java
    `maven-publish`
}

group =
    extra["project.group"]?.toString() ?: throw IllegalArgumentException("The project group has not been set.")
version =
    extra["project.version"]?.toString() ?: throw IllegalArgumentException("The project version has not been set.")

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")

    compileOnly("org.apache.logging.log4j:log4j-api:2.8.1")
    compileOnly("org.slf4j:slf4j-api:1.8.0-beta4")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId =
            extra["project.name"]?.toString()
                ?: throw IllegalArgumentException("The project name has not been set.")
        groupId = project.group.toString()
        version = project.version.toString()

        from(components["java"])
    }

    repositories {
        if (project.hasProperty("unifycraft.publishing.username") && project.hasProperty("unifycraft.publishing.password")) {
            fun MavenArtifactRepository.applyCredentials() {
                authentication.create<BasicAuthentication>("basic")
                credentials {
                    username = property("unifycraft.publishing.username")?.toString()
                    password = property("unifycraft.publishing.password")?.toString()
                }
            }

            maven {
                name = "UnifyCraftRelease"
                url = uri("https://maven.unifycraft.xyz/releases")
                applyCredentials()
            }

            maven {
                name = "UnifyCraftSnapshots"
                url = uri("https://maven.unifycraft.xyz/snapshots")
                applyCredentials()
            }
        }
    }
}
