package xyz.spruceloader.launchwrapper;

import xyz.spruceloader.launchwrapper.api.ArgumentMap;
import xyz.spruceloader.launchwrapper.api.EnvSide;

public class MainServer {
    public static void main(String[] args) {
        ArgumentMap argMap = ArgumentMap.parse(args);
        argMap.remove("version");
        argMap.remove("gameDir");
        argMap.remove("assetsDir");
        Launch.getInstance().initialize(argMap, EnvSide.SERVER);
    }
}
