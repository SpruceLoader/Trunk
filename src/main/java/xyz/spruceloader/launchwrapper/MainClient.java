package xyz.spruceloader.launchwrapper;

import xyz.spruceloader.launchwrapper.api.ArgumentMap;
import xyz.spruceloader.launchwrapper.api.EnvSide;

import java.nio.file.Paths;

public class MainClient {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.putIfAbsent("accessToken", "None");
        argMap.putIfAbsent("version", "Unknown");
        argMap.putIfAbsent("gameDir", Paths.get(".").normalize().toString());
        Launch.getInstance().initialize(argMap, EnvSide.CLIENT);
    }
}
