package xyz.spruceloader.launchwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.spruceloader.launchwrapper.api.ArgumentMap;
import xyz.spruceloader.launchwrapper.api.LaunchTransformers;
import xyz.spruceloader.launchwrapper.api.EnvSide;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Launch {
    public static final boolean DEVELOPMENT = Boolean.getBoolean("launch.development");
    private static final Logger LOGGER = LoggerFactory.getLogger("Launchwrapper");

    private Map<String, Object> globalProperties = new HashMap<>();
    private LaunchClassLoader classLoader;
    private final List<Path> classPath = new ArrayList<>();

    public Launch() {
        setupClassPath();
        classLoader = new LaunchClassLoader(classPath.stream().map(path -> {
            try {
                return path.toUri().toURL();
            } catch (Throwable t) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()), getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        globalProperties.put("launch.development", DEVELOPMENT);
    }

    public void initialize(ArgumentMap argMap, EnvSide env) {
        LOGGER.info("Launching Minecraft with Spruce Launchwrapper");

        LaunchTransformers.addTransformer(new InternalLaunchTransformer());
        LaunchTransformers.initialize(argMap);
        LaunchTransformers.performTask(transformer -> {
            transformer.takeArguments(argMap, env);
            transformer.injectIntoClassLoader(classLoader);

            String transformerClassName = transformer.getClass().getName();
            classLoader.addClassLoaderException(transformerClassName.contains(".") ? transformerClassName.substring(0, transformerClassName.lastIndexOf('.')) : transformerClassName);
        });

        launch(argMap.toArray(), env);
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
            LOGGER.warn("UniLoader Launch does not support wildcard class path entries. The game may not load properly.\n{}", String.join("\n", unsupportedEntries));
        if (!missingEntries.isEmpty())
            LOGGER.warn("Class-path entries reference missing files! The game may not load properly.\n{}", String.join("\n", missingEntries));
    }

    private void launch(String[] args, EnvSide env) {
        try {
            Class<?> clz = Class.forName(env.getLaunchClass(), false, classLoader);
            MethodHandle handle = MethodHandles.publicLookup().findStatic(clz, "main", MethodType.methodType(void.class, String[].class));
            handle.invoke((Object) args);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to launch Minecraft!", t);
        }
    }

    public Map<String, Object> getGlobalProperties() {
        return globalProperties;
    }

    public LaunchClassLoader getClassLoader() {
        return classLoader;
    }

    public List<Path> getClassPath() {
        return Collections.unmodifiableList(classPath);
    }

    public void addToClassPath(Path path) {
        if (classLoader != null) classLoader.addPath(path);
        classPath.add(path);
    }
}
