package xyz.spruceloader.trunk.api;

public enum EnvSide {
    CLIENT("net.minecraft.client.main.Main"),
    SERVER("net.minecraft.server.Main");

    private final String launchClass;

    EnvSide(String launchClass) {
        this.launchClass = launchClass;
    }

    public String getLaunchClass() {
        return launchClass;
    }
}
