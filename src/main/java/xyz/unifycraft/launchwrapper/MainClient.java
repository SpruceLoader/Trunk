package xyz.unifycraft.launchwrapper;

import xyz.unifycraft.launchwrapper.api.ArgumentMap;
import xyz.unifycraft.launchwrapper.api.EnvSide;

import java.io.File;

public class MainClient {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.putIfAbsent("accessToken", "None");
        argMap.putIfAbsent("version", "Unknown");
        argMap.putIfAbsent("gameDir", new File(".").getAbsolutePath());
        Launch.getInstance().initialize(argMap, EnvSide.CLIENT);
    }
}
