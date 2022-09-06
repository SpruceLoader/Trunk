import net.fabricmc.loom.LoomGradleExtension

plugins {
    java
    id("xyz.unifycraft.uniloom") version("1.0.0-beta.16")
}

val launch by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
    configurations.modCompileClasspath.get().extendsFrom(this)
}

if (loom is LoomGradleExtension)
    (loom as LoomGradleExtension).useInstallerData("1", file("installer-data.json"))

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net")
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.2")
    mappings("net.fabricmc:yarn:1.19.2+build.8")

    launch(project(":"))
}
