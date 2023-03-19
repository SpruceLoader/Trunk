plugins {
    java
    `maven-publish`
}

group = extra["project.group"]?.toString() ?: throw IllegalArgumentException("The project group has not been set.")
version = extra["project.version"]?.toString() ?: throw IllegalArgumentException("The project version has not been set.")

val gitBranch = System.getenv("GITHUB_REF_NAME")
val gitCommit = System.getenv("GITHUB_SHA")
if (gitBranch != null && gitCommit != null) {
    val shortenedCommit = gitCommit.substring(0, 7)
    version = "$version-SNAPSHOT+$gitBranch-$shortenedCommit"
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.ow2.asm:asm-tree:9.4")
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
        val publishingUsername = project.findProperty("spruceloader.publishing.username")?.toString() ?: System.getenv("SPRUCELOADER_PUBLISHING_USERNAME")
        val publishingPassword = project.findProperty("spruceloader.publishing.password")?.toString() ?: System.getenv("SPRUCELOADER_PUBLISHING_PASSWORD")
        if (publishingUsername != null && publishingPassword != null) {
            fun MavenArtifactRepository.applyCredentials() {
                authentication.create<BasicAuthentication>("basic")
                credentials {
                    username = publishingUsername
                    password = publishingPassword
                }
            }

            maven {
                name = "SpruceReleases"
                url = uri("https://maven.spruceloader.xyz/releases")
                applyCredentials()
            }

            maven {
                name = "SpruceSnapshots"
                url = uri("https://maven.spruceloader.xyz/snapshots")
                applyCredentials()
            }
        }
    }
}
