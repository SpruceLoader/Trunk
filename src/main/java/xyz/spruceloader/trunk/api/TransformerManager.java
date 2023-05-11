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

package xyz.spruceloader.trunk.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidClassException;
import java.util.*;

public class TransformerManager implements Iterable<Transformer> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransformerManager.class);

    private final List<Transformer> transformers = new ArrayList<>();
    private boolean initialized = false;

    public void initialize(ArgumentMap argMap) {
        if (initialized)
            throw new IllegalStateException("Cannot initialize launch" +
                    " listeners twice!");

        handleFromNamespaces(argMap, "trunkTransformer", "trunk.transformers");
        initialized = true;
    }

    public void addTransformer(Transformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public Iterator<Transformer> iterator() {
        return transformers.iterator();
    }

    public List<Transformer> getTransformers() {
        return transformers;
    }

    private void handleFromNamespaces(ArgumentMap argMap, String argName, String propName) {
        argMap.getAll(argName).forEach(
                name -> transformers.add(fromName(name)));

        String prop = System.getProperty(propName);
        if (prop != null)
            Arrays.stream(prop.split("/")).forEach(
                    name -> transformers.add(fromName(name)));

        transformers.removeIf(Objects::isNull);
        argMap.remove(argName);
    }

    private Transformer fromName(String name) {
        ClassLoader loader = TransformerManager.class.getClassLoader();
        try {
            Class<?> clz = Class.forName(name, true, loader);
            if (!Transformer.class.isAssignableFrom(clz))
                throw new InvalidClassException("The class provided isn't a" +
                        " launch transformer!");
            return (Transformer) clz.getConstructor().newInstance();
        } catch (InvalidClassException e) {
            LOGGER.error("There was an invalid launch listener! ({})", name, e);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch launch listener... ({})", name, e);
        }

        return null;
    }
}
