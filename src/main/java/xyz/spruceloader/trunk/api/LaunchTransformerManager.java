package xyz.spruceloader.trunk.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LaunchTransformerManager implements Iterable<LaunchTransformer> {

    private static final Logger LOGGER = LogManager.getLogger("LaunchTransformerManager");

    private boolean initialized = false;
    private final List<LaunchTransformer> transformers = new ArrayList<>();

    public void initialize(ArgumentMap argMap) {
        if (initialized)
            throw new IllegalStateException("Cannot initialize launch listeners twice!");

        handleFromNamespaces(argMap, "trunkTransformer", "trunk.transformers");
        initialized = true;
    }

    public void addTransformer(LaunchTransformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public Iterator<LaunchTransformer> iterator() {
        return transformers.iterator();
    }

    public List<LaunchTransformer> getTransformers() {
        return transformers;
    }

    private void handleFromNamespaces(ArgumentMap argMap, String argName, String propName) {
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

    private LaunchTransformer fromName(String name) {
        try {
            Class<?> clz = Class.forName(name, true, LaunchTransformerManager.class.getClassLoader());
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
