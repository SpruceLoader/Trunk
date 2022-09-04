package xyz.unifycraft.launchwrapper.api;

public interface LaunchListener {
    void takeArguments(ArgumentMap argMap, EnvSide env);
    void setupPostClassPath();
}
