package xyz.unifycraft.launchwrapper;

import org.apache.commons.io.IOUtils;
import xyz.unifycraft.launchwrapper.api.LaunchTransformer;
import xyz.unifycraft.launchwrapper.api.LaunchTransformers;
import xyz.unifycraft.launchwrapper.exceptions.LoadingException;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

public class LaunchClassLoader extends URLClassLoader {
    private final Set<String> classLoaderExceptions = new HashSet<>();
    private final Set<String> transformationExceptions = new HashSet<>();

    private final Set<String> badClasses = new HashSet<>();
    private final Map<String, Class<?>> classCache = new HashMap<>();

    public LaunchClassLoader(List<URL> urls, ClassLoader parent) {
        super(urls.toArray(new URL[0]), parent);

        addClassLoaderException("java.");
        addClassLoaderException("jdk.");
        addClassLoaderException("javax.");
        addClassLoaderException("sun.");
        addClassLoaderException("com.sun.");
        addClassLoaderException("org.xml.");
        addClassLoaderException("org.w3c.");
        addClassLoaderException("org.apache.");
        addClassLoaderException("org.slf4j.");
        addClassLoaderException("com.mojang.blocklist.");

        addClassLoaderException("xyz.unifycraft.launchwrapper.");
    }

    protected void addPath(Path path) {
        try {
            super.addURL(path.toUri().toURL());
        } catch (Exception e) {
            throw new LoadingException("Failed to load an item to the classpath!", e);
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (classLoaderExceptions.stream().anyMatch(name::startsWith))
            return getParent().loadClass(name);

        if (classCache.containsKey(name))
            return classCache.get(name);

        for (String exception : transformationExceptions) {
            if (name.startsWith(exception)) {
                try {
                    Class<?> clz = super.findClass(name);
                    classCache.put(name, clz);
                    return clz;
                } catch (Exception e) {
                    badClasses.add(name);
                    throw e;
                }
            }
        }

        try {
            Class<?> clz = findLoadedClass(name);
            if (clz == null) {
                byte[] data = fetchModifiedClass(name);
                clz = defineClass(name, data, 0, data.length);
            }

            return clz;
        } catch (Throwable t) {
            badClasses.add(name);
            throw new ClassNotFoundException(name, t);
        }
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clz = loadClass(name);
        if (resolve) resolveClass(clz);
        return clz;
    }

    private byte[] fetchModifiedClass(String name) throws ClassNotFoundException {
        byte[] data = loadClassData(name);
        for (LaunchTransformer transformer : LaunchTransformers.getTransformers()) data = transformer.transform(name, data);
        return data;
    }

    private byte[] loadClassData(String name) throws ClassNotFoundException {
        try {
            Enumeration<URL> resources = getResources(name.replace('.', '/') + ".class");

            List<String> locations = new ArrayList<>();
            List<byte[]> data = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                locations.add(resource.toString());
                data.add(IOUtils.toByteArray(resource));
            }

            return data.toArray(new byte[0][])[0];
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    public void addClassLoaderException(String className) {
        classLoaderExceptions.add(className);
    }

    public void addTransformationException(String className) {
        transformationExceptions.add(className);
    }
}
