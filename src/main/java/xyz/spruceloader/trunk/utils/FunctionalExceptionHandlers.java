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

import java.util.function.Consumer;
import java.util.function.Function;

public class FunctionalExceptionHandlers {
    public static Runnable unexcept(ThrowingRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                _throw(t);
            }
        };
    }

    public static <T> Consumer<T> unexceptConsumer(ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable t2) {
                _throw(t2);
            }
        };
    }

    public static <T, R> Function<T, R> unexcept(ThrowingFunction<T, R> function) {
        return (t) -> {
            try {
                return function.apply(t);
            } catch (Throwable t2) {
                _throw(t2);
                return null;
            }
        };
    }

    public static <T> T unexceptSupplier(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            _throw(t);
            return null;
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void _throw(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
