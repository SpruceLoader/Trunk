package xyz.unifycraft.launchwrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.unifycraft.launchwrapper.api.ArgumentMap;
import xyz.unifycraft.launchwrapper.api.EnvSide;
import xyz.unifycraft.launchwrapper.api.LaunchListener;
import xyz.unifycraft.launchwrapper.api.LaunchListeners;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Launch {
    private static Launch INSTANCE;
    private static Logger logger = LogManager.getLogger("Launchwrapper");

    private LaunchClassLoader classLoader;
    private final List<Path> classPath = new ArrayList<>();

    public void initialize(ArgumentMap argMap, EnvSide env) {
        String[] argsArray = argMap.toArray();
        logger.info("Launching Minecraft with UniLoader Launchwrapper");

        classLoader = new LaunchClassLoader(getClass().getClassLoader());
        setupClassPath();

        LaunchListeners.initialize(argMap);
        LaunchListeners.performTask(listener -> listener.takeArguments(argMap, env));

        launch(argsArray, env);

        LaunchListeners.performTask(LaunchListener::setupPostClassPath);
    }

    private void setupClassPath() {
        classPath.clear();
        List<String> unsupportedEntries = new ArrayList<>();
        List<String> missingEntries = new ArrayList<>();

        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (entry.equals("*") || entry.endsWith(File.pathSeparator + "*")) {
                unsupportedEntries.add(entry);
                continue;
            }

            Path path = Paths.get(entry);
            if (!Files.exists(path)) {
                missingEntries.add(entry);
                continue;
            }

            addToClassPath(path);
        }

        if (!unsupportedEntries.isEmpty())
            logger.warn("UniLoader Launch does not support wildcard class path entries. The game may not load properly.\n{}", String.join("\n", unsupportedEntries));
        if (!missingEntries.isEmpty())
            logger.warn("Class-path entries reference missing files! The game may not load properly.\n{}", String.join("\n", missingEntries));
    }

    private void launch(String[] args, EnvSide env) {
        try {
            Class<?> clz = classLoader.loadClass(env.getLaunchClass());
            MethodHandle handle = MethodHandles.lookup().findStatic(clz, "main", MethodType.methodType(void.class, String[].class));
            handle.invokeExact(args);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to launch Minecraft!", t);
        }
    }

    public List<Path> getClassPath() {
        return Collections.unmodifiableList(classPath);
    }

    public void addToClassPath(Path path) {
        classLoader.addPath(path);
        classPath.add(path);
    }

    public static Launch getInstance() {
        if (INSTANCE == null) // Lazy-load the class
            INSTANCE = new Launch();
        return INSTANCE;
    }
}
