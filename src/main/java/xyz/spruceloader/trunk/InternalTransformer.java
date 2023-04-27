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

import org.objectweb.asm.*;

import xyz.spruceloader.trunk.api.Transformer;
import xyz.spruceloader.trunk.utils.MappingConfiguration;

class InternalTransformer implements Transformer {

    /**
     * Adapted from Fabric Loader under Apache License 2.0
     */
    public byte[] transform(String className, byte[] rawClass) {
        boolean isMinecraftClass = className.startsWith("net.minecraft.")
                || className.startsWith("com.mojang.blaze3d.")
                || className.indexOf('.') < 0;
        boolean mustTransformAccess = isMinecraftClass
                && MappingConfiguration.INSTANCE.requiresPackageAccessHack();
        if (!mustTransformAccess)
            return rawClass;

        ClassReader classReader = new ClassReader(rawClass);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassVisitor visitor = classWriter;

        visitor = new PackageAccessFixer(Opcodes.ASM9, visitor);
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }

    /**
     * Adapted from Fabric Loader under Apache License 2.0
     * <p>
     * Changes package-private and protected access flags to public. In a
     * development environment, Minecraft classes may be mapped into a package
     * structure with invalid access across packages. The class verifier will
     * complain unless we simply change package-private and protected to public.
     */
    private static class PackageAccessFixer extends ClassVisitor {
        private static int modAccess(int access) {
            if ((access & 0x7) != Opcodes.ACC_PRIVATE) {
                return (access & (~0x7)) | Opcodes.ACC_PUBLIC;
            } else {
                return access;
            }
        }

        public PackageAccessFixer(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        public void visit(int version, int access, String name, String signature, String superName,
                          String[] interfaces) {
            super.visit(version, modAccess(access), name, signature, superName, interfaces);
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, modAccess(access));
        }

        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return super.visitField(modAccess(access), name, descriptor, signature, value);
        }

        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                         String[] exceptions) {
            return super.visitMethod(modAccess(access), name, descriptor, signature, exceptions);
        }
    }
}
