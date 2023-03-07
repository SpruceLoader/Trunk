package xyz.spruceloader.launchwrapper;

import org.apache.commons.io.IOUtils;
import xyz.spruceloader.launchwrapper.api.*;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class LaunchClassLoader extends URLClassLoader {

    private final List<Predicate<String>> filters = new ArrayList<>();
    private final ClassLoader fallback;

    public LaunchClassLoader(URL[] urls, ClassLoader fallback) {
        super(urls, null);
        this.fallback = fallback;
    }

    public void addPath(Path path) {
        try {
            addURL(path.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(path.toString(), e);
        }
    }

    /**
     * Adds a loading filter - returns true to filter the class.
     *
     * @param filter the filter.
     */
    public void addLoadingFilter(Predicate<String> filter) {
        filters.add(filter);
    }

    /**
     * Filters a package out from loading.
     *
     * @param packageName the package name.
     */
    public void addPackageLoadingFilter(String packageName) {
        String suffixed = packageName + '.';
        filters.add(name -> name.startsWith(suffixed));
    }

    /**
     * Filters a class out from loading.
     *
     * @param className the class name.
     */
    public void addClassLoadingFilter(String className) {
        filters.add(className::equals);
    }

    private boolean filter(String className) {
        return filters.stream().anyMatch((filter) -> filter.test(className));
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded != null)
                return loaded;

            if (filter(name))
                return fallback.loadClass(name);

            Class<?> result = findClass(name);
            if (result == null)
                return fallback.loadClass(name);

            return result;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = transformClassBytes(name);
            if (data == null)
                throw new ClassNotFoundException(name);

            return defineClass(name, data, 0, data.length);
        } catch (Throwable e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] transformClassBytes(String name) throws IOException {
        return transformClassBytes(name, getClassBytes(name));
    }

    private byte[] transformClassBytes(String name, byte[] bytes) {
        for (LaunchTransformer transformer : LaunchTransformers.getTransformers())
            bytes = transformer.transform(name, bytes);

        return bytes;
    }

    private byte[] getClassBytes(String name) throws IOException {
        try (InputStream in = getResourceAsStream(name)) {
            if (in == null)
                return null;

            return IOUtils.toByteArray(in);
        }
    }

}
