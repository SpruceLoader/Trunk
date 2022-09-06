pluginManagement {
    repositories {
        // Default repositories
        gradlePluginPortal()
        mavenCentral()

        // Repositories
        maven("https://maven.unifycraft.xyz/releases")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://jitpack.io/")

        // Snapshots
        maven("https://maven.unifycraft.xyz/snapshots")
        mavenLocal()
    }
}

rootProject.name =
    extra["project.name"]?.toString() ?: throw IllegalArgumentException("The project name has not been set.")

include(":minecraft-test")
