package xyz.spruceloader.trunk;

import java.nio.file.Paths;

import xyz.spruceloader.trunk.api.ArgumentMap;
import xyz.spruceloader.trunk.api.EnvSide;

public class MainClient {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.putIfAbsent("accessToken", "None");
        argMap.putIfAbsent("version", "Unknown");
        argMap.putIfAbsent("gameDir", Paths.get(".").normalize().toString());

        Trunk trunk = new Trunk();
        trunk.initialize(argMap, EnvSide.CLIENT);
        trunk.getClassLoader().addDefaultLoadingFilters();
    }
}
