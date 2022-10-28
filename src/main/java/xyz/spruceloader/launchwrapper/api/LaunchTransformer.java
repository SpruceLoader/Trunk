package xyz.spruceloader.launchwrapper.api;

import xyz.spruceloader.launchwrapper.LaunchClassLoader;

public interface LaunchTransformer {
    void takeArguments(ArgumentMap argMap, EnvSide env);
    void injectIntoClassLoader(LaunchClassLoader classLoader);
    default byte[] transform(String className, byte[] rawClass) {
        return rawClass;
    }
}
