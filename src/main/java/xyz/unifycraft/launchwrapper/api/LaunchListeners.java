package xyz.unifycraft.launchwrapper.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LaunchListeners {
    private static boolean initialized = false;
    private static Logger logger = LogManager.getLogger("LaunchListeners");

    private static List<LaunchListener> listeners;

    public static void initialize(ArgumentMap argMap) {
        if (initialized)
            throw new IllegalStateException("Cannot initialize launch listeners twice!");

        listeners = new ArrayList<>();

        List<String> argValues = argMap.getAll("launchListener");
        if (argValues != null) argValues.forEach(name -> listeners.add(fromName(name)));

        String prop = System.getProperty("unilaunchwrapper.listeners");
        if (prop != null) {
            List<String> propValues = Arrays.stream(prop.split("/")).toList();
            if (!propValues.isEmpty()) propValues.forEach(name -> listeners.add(fromName(name)));
        }

        listeners = listeners.stream().filter(Objects::nonNull).collect(Collectors.toList());

        initialized = true;
    }

    public static void addListener(LaunchListener listener) {
        if (listeners == null) throw new IllegalStateException("Somehow called too early?");
        listeners.add(listener);
    }

    public static void performTask(Consumer<LaunchListener> consumer) {
        listeners.forEach(consumer);
    }

    public static List<LaunchListener> getListeners() {
        return listeners;
    }

    private static LaunchListener fromName(String name) {
        try {
            Class<?> clz = Class.forName(name, true, LaunchListeners.class.getClassLoader());
            if (!clz.isAssignableFrom(LaunchListener.class)) throw new InvalidClassException("The class provided isn't a launch listener!");
            LaunchListener instance = (LaunchListener) clz.getConstructor().newInstance();
            return instance;
        } catch (InvalidClassException e) {
            logger.error("There was an invalid launch listener! ({})", name, e);
        } catch (Exception e) {
            logger.error("Failed to fetch launch listener... ({})", name, e);
        }

        return null;
    }
}
