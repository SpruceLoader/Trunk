package xyz.spruceloader.trunk;

import xyz.spruceloader.trunk.api.ArgumentMap;
import xyz.spruceloader.trunk.api.EnvSide;

public class MainServer {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.remove("version");
        argMap.remove("gameDir");
        argMap.remove("assetsDir");
        new Trunk().initialize(argMap, EnvSide.SERVER);
    }
}
