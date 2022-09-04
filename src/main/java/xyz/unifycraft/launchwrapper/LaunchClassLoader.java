package xyz.unifycraft.launchwrapper;

import xyz.unifycraft.launchwrapper.exceptions.LoadingException;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class LaunchClassLoader extends URLClassLoader {
    public LaunchClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    protected void addPath(Path path) {
        try {
            super.addURL(path.toUri().toURL());
        } catch (Exception e) {
            throw new LoadingException("Failed to load an item to the classpath!", e);
        }
    }
}
