package xyz.spruceloader.launchwrapper.api;

import xyz.spruceloader.launchwrapper.LaunchClassLoader;

public interface LaunchTransformer {

    default void takeArguments(ArgumentMap argMap, EnvSide env) {
    }

    default void injectIntoClassLoader(LaunchClassLoader classLoader) {
    }

    default byte[] transform(String className, byte[] rawClass) {
        return rawClass;
    }
}
