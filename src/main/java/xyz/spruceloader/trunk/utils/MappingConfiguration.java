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

package xyz.spruceloader.trunk.utils;

import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import xyz.spruceloader.trunk.Trunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipError;

/**
 * Adapted from Fabric Loader under Apache License 2.0
 */
public final class MappingConfiguration {
    private static MappingConfiguration INSTANCE;

    private String gameId;
    private String gameVersion;
    private TinyTree mappings;

    private MappingConfiguration() {
        initialize();
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public boolean matches(String gameId, String gameVersion) {
        return (this.gameId == null || gameId == null || gameId.equals(this.gameId))
                && (this.gameVersion == null || gameVersion == null || gameVersion.equals(this.gameVersion));
    }

    public TinyTree getMappings() {
        return mappings;
    }

    public String getTargetNamespace() {
        return Trunk.DEVELOPMENT ? "named" : "intermediary";
    }

    public boolean requiresPackageAccessHack() {
        // TODO
        return getTargetNamespace().equals("named");
    }

    private void initialize() {
        URL url = MappingConfiguration.class.getClassLoader().getResource("mappings/mappings.tiny");

        if (url != null) {
            try {
                URLConnection connection = url.openConnection();

                if (connection instanceof JarURLConnection) {
                    Manifest manifest = ((JarURLConnection) connection).getManifest();

                    if (manifest != null) {
                        gameId = getManifestValue(manifest, new Name("Game-Id"));
                        gameVersion = getManifestValue(manifest, new Name("Game-Version"));
                    }
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    mappings = TinyMappingFactory.loadWithDetection(reader);
                }
            } catch (IOException | ZipError e) {
                throw new RuntimeException("Error reading " + url, e);
            }
        }

        if (mappings == null)
            mappings = TinyMappingFactory.EMPTY_TREE;
    }

    private static String getManifestValue(Manifest manifest, Name name) {
        return manifest.getMainAttributes().getValue(name);
    }

    public static MappingConfiguration getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new MappingConfiguration();
        }
        return INSTANCE;
    }
}
