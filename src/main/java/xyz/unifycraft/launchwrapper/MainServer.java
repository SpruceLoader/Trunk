package xyz.unifycraft.launchwrapper;

import xyz.unifycraft.launchwrapper.api.ArgumentMap;
import xyz.unifycraft.launchwrapper.api.EnvSide;

public class MainServer {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.remove("version");
        argMap.remove("gameDir");
        argMap.remove("assetsDir");
        Launch.getInstance().initialize(argMap, EnvSide.SERVER);
    }
}
