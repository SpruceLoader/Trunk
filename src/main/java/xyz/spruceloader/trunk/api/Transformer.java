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

import xyz.spruceloader.trunk.Trunk;
import xyz.spruceloader.trunk.TrunkClassLoader;

public interface Transformer {

    default void initialize(Trunk trunk) {
    }

    default void takeArguments(ArgumentMap argMap, EnvSide env) {
    }

    default void injectIntoClassLoader(TrunkClassLoader classLoader) {
    }

    default byte[] transform(String className, byte[] rawClass) {
        return rawClass;
    }
}
