package xyz.unifycraft.launchwrapper.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LaunchTransformers {
    private static boolean initialized = false;
    private static final Logger LOGGER = LogManager.getLogger("LaunchTransformers");

    private static final List<LaunchTransformer> transformers = new CopyOnWriteArrayList<>();

    public static void initialize(ArgumentMap argMap) {
        if (initialized)
            throw new IllegalStateException("Cannot initialize launch listeners twice!");

        handleFromNamespaces(argMap, "launchListener", "unilaunchwrapper.listeners");
        handleFromNamespaces(argMap, "launchTransformer", "unilaunchwrapper.transformers");

        initialized = true;
    }

    public static void addTransformer(LaunchTransformer transformer) {
        transformers.add(transformer);
    }

    public static void performTask(Consumer<LaunchTransformer> consumer) {
        transformers.forEach(consumer);
    }

    public static List<LaunchTransformer> getTransformers() {
        return transformers;
    }

    private static void handleFromNamespaces(ArgumentMap argMap, String argName, String propName) {
        List<String> argValues = argMap.getAll(argName);
        if (argValues != null) argValues.forEach(name -> transformers.add(fromName(name)));

        String prop = System.getProperty(propName);
        if (prop != null) {
            List<String> propValues = Arrays.stream(prop.split("/")).toList();
            if (!propValues.isEmpty()) propValues.forEach(name -> transformers.add(fromName(name)));
        }

        transformers.removeIf(Objects::isNull);
        argMap.remove(argName);
    }

    private static LaunchTransformer fromName(String name) {
        try {
            Class<?> clz = Class.forName(name, true, LaunchTransformers.class.getClassLoader());
            if (!LaunchTransformer.class.isAssignableFrom(clz)) throw new InvalidClassException("The class provided isn't a launch transformer!");
            LaunchTransformer instance = (LaunchTransformer) clz.getConstructor().newInstance();
            return instance;
        } catch (InvalidClassException e) {
            LOGGER.error("There was an invalid launch listener! ({})", name, e);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch launch listener... ({})", name, e);
        }

        return null;
    }
}
