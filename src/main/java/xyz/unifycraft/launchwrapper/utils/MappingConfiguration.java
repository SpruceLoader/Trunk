package xyz.unifycraft.launchwrapper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipError;

import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import xyz.unifycraft.launchwrapper.Launch;

/**
 * Adapted from Fabric Loader under Apache License 2.0
 */
public final class MappingConfiguration {
    private static MappingConfiguration INSTANCE;

    private boolean initialized;

    private String gameId;
    private String gameVersion;
    private TinyTree mappings;

    public String getGameId() {
        initialize();

        return gameId;
    }

    public String getGameVersion() {
        initialize();

        return gameVersion;
    }

    public boolean matches(String gameId, String gameVersion) {
        initialize();

        return (this.gameId == null || gameId == null || gameId.equals(this.gameId))
                && (this.gameVersion == null || gameVersion == null || gameVersion.equals(this.gameVersion));
    }

    public TinyTree getMappings() {
        initialize();

        return mappings;
    }

    public String getTargetNamespace() {
        return Launch.getInstance().isDevelopment() ? "named" : "intermediary";
    }

    public boolean requiresPackageAccessHack() {
        // TODO
        return getTargetNamespace().equals("named");
    }

    private void initialize() {
        if (initialized) return;

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

        initialized = true;
    }

    private static String getManifestValue(Manifest manifest, Name name) {
        return manifest.getMainAttributes().getValue(name);
    }

    public static MappingConfiguration getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MappingConfiguration();
        return INSTANCE;
    }
}
