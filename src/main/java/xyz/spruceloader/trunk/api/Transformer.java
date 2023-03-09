package xyz.spruceloader.trunk.api;

import xyz.spruceloader.trunk.TrunkClassLoader;

public interface Transformer {

    default void takeArguments(ArgumentMap argMap, EnvSide env) {
    }

    default void injectIntoClassLoader(TrunkClassLoader classLoader) {
    }

    default byte[] transform(String className, byte[] rawClass) {
        return rawClass;
    }
}
