package xyz.unifycraft.launchwrapper.api;

public interface LaunchTransformer {
    void takeArguments(ArgumentMap argMap, EnvSide env);
    default byte[] transform(String className, byte[] rawClass) {
        return rawClass;
    }
}
