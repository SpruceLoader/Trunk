package xyz.unifycraft.launchwrapper.api;

public interface LaunchTransformer {
    void takeArguments(ArgumentMap argMap, EnvSide env);
    void setupPostClassPath();
    byte[] transform(String className, byte[] rawClass);
}
