package xyz.spruceloader.trunk;

import org.objectweb.asm.*;

import xyz.spruceloader.trunk.api.LaunchTransformer;
import xyz.spruceloader.trunk.utils.MappingConfiguration;

class InternalLaunchTransformer implements LaunchTransformer {

    /**
     * Adapted from Fabric Loader under Apache License 2.0
     */
    public byte[] transform(String className, byte[] rawClass) {
        boolean isMinecraftClass = className.startsWith("net.minecraft.") || className.startsWith("com.mojang.blaze3d.")
                || className.indexOf('.') < 0;
        boolean mustTransformAccess = isMinecraftClass
                && MappingConfiguration.getInstance().requiresPackageAccessHack();
        if (!mustTransformAccess)
            return rawClass;

        ClassReader classReader = new ClassReader(rawClass);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassVisitor visitor = classWriter;
        int visitorCount = 0;

        if (mustTransformAccess) {
            visitor = new PackageAccessFixer(Opcodes.ASM9, visitor);
            visitorCount++;
        }

        if (visitorCount <= 0)
            return rawClass;
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }

    /**
     * Adapted from Fabric Loader under Apache License 2.0
     *
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
