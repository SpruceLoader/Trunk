/*
 * Trunk, the Spruce service used to launch Minecraft
 * Copyright (C) 2023  SpruceLoader
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package xyz.spruceloader.trunk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.spruceloader.trunk.api.ArgumentMap;
import xyz.spruceloader.trunk.api.EnvSide;
import xyz.spruceloader.trunk.api.TransformerManager;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static xyz.spruceloader.trunk.utils.FunctionalExceptionHandlers.unexcept;

public class Trunk {
    public static final boolean DEVELOPMENT = Boolean.getBoolean("trunk.development");
    public static final Map<String, Object> GLOBAL_PROPERTIES = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("Trunk");

    private final TrunkClassLoader classLoader;
    private final TransformerManager transformerManager;
    private final List<Path> classPath = new ArrayList<>();

    public Trunk() {
        setupClassPath();
        classLoader = new TrunkClassLoader(
                this,
                classPath.stream()
                        .map(unexcept(path -> path.toUri().toURL()))
                        .toArray(URL[]::new),
                getClass().getClassLoader()
        );
        transformerManager = new TransformerManager();
        Thread.currentThread().setContextClassLoader(classLoader);
        GLOBAL_PROPERTIES.put("trunk.development", DEVELOPMENT);
    }

    public void initialize(ArgumentMap argMap, EnvSide env) {
        LOGGER.info("Launching Minecraft with Spruce Trunk");

        transformerManager.addTransformer(new InternalTransformer());
        transformerManager.initialize(argMap);
        transformerManager.forEach(transformer -> {
            transformer.initialize(this);
            transformer.takeArguments(argMap, env);
            transformer.injectIntoClassLoader(classLoader);

            String transformerClassName = transformer.getClass().getName();

            if (transformerClassName.indexOf(".") != 0)
                classLoader.addPackageLoadingFilter(
                        transformerClassName.substring(0, transformerClassName.lastIndexOf('.')));
            else
                classLoader.addClassLoadingFilter(transformerClassName);
        });

        launch(argMap, env);
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
            LOGGER.warn("Trunk does not support wildcard class path entries. The game may not load properly.\n{}",
                    String.join("\n", unsupportedEntries));
        if (!missingEntries.isEmpty())
            LOGGER.warn("Class-path entries reference missing files! The game may not load properly.\n{}",
                    String.join("\n", missingEntries));
    }

    private void launch(ArgumentMap argMap, EnvSide env) {
        try {
            String mainClass = argMap.get("trunkMainClass").orElseGet(env::getLaunchClass);
            Optional<String> fromProperty = Optional.ofNullable(System.getProperty("trunk.mainClass"));
            if (fromProperty.isPresent()) {
                mainClass = fromProperty.get();
            }

            Class<?> clz = Class.forName(mainClass, false, classLoader);
            MethodHandle handle = MethodHandles.publicLookup().findStatic(clz, "main",
                    MethodType.methodType(void.class, String[].class));
            handle.invoke((Object) argMap.toArray());
        } catch (Throwable t) {
            throw new RuntimeException("Failed to launch Minecraft!", t);
        }
    }

    public TrunkClassLoader getClassLoader() {
        return classLoader;
    }

    public TransformerManager getTransformerManager() {
        return transformerManager;
    }

    public List<Path> getClassPath() {
        return Collections.unmodifiableList(classPath);
    }

    public void addToClassPath(Path path) {
        if (classLoader != null)
            classLoader.addPath(path);
        classPath.add(path);
    }
}
