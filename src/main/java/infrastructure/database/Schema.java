package infrastructure.database;

import infrastructure.io.ByteSerializable;
import infrastructure.utility.ReflectionUtility;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dynamically compiles and instantiates proxy classes that serialize objects into byte arrays using reflection.
 * The generated proxy extends the provided class, overriding setters to update a ByteBuffer.
 */
public class Schema {

    /**
     * Record to hold code template and size for serialization.
     */
    private record Template(String codeSetTemplate, String codeGetTemplate, int size) {
    }

    /**
     * Mapping of supported types to their serialization templates.
     */
    private static final Map<Class<?>, Template> TEMPLATES = new HashMap<>();
    private static final Map<Class<?>, Object> GENERATED = new HashMap<>();

    static {
        TEMPLATES.put(byte.class, new Template("buffer.put(%s, (byte) %s);", "buffer.get(%s)", 1));
        TEMPLATES.put(short.class, new Template("buffer.putShort(%s, (short) %s);", "buffer.getShort(%s)", 2));
        TEMPLATES.put(int.class, new Template("buffer.putInt(%s, %s);", "buffer.getInt(%s)", 4));
        TEMPLATES.put(long.class, new Template("buffer.putLong(%s, %sL);", "buffer.getLong(%s)", 8));
        TEMPLATES.put(float.class, new Template("buffer.putFloat(%s, %sf);", "buffer.getFloat(%s)", 4));
        TEMPLATES.put(double.class, new Template("buffer.putDouble(%s, %s);", "buffer.getDouble(%s)", 8));
        TEMPLATES.put(boolean.class, new Template("buffer.put(%s, (byte) (%s ? 1 : 0));", "buffer.get(%s) == 0 ? false : true", 1));
        TEMPLATES.put(char.class, new Template("buffer.putChar(%s, (char) %s);", "buffer.getChar(%s)", 2));
        TEMPLATES.put(String.class, new Template("buffer.put(%s, %s.getBytes());", "int length = %s;\nbyte[length] strBytes = new byte[length];\nbuffer.get(strBytes);", 240));
        TEMPLATES.put(LocalDate.class, new Template("buffer.putLong(%s, %s.toEpochDay());", "", Long.BYTES));

        TEMPLATES.put(Byte.class, TEMPLATES.get(byte.class));
        TEMPLATES.put(Short.class, TEMPLATES.get(short.class));
        TEMPLATES.put(Integer.class, TEMPLATES.get(int.class));
        TEMPLATES.put(Long.class, TEMPLATES.get(long.class));
        TEMPLATES.put(Float.class, TEMPLATES.get(float.class));
        TEMPLATES.put(Double.class, TEMPLATES.get(double.class));
        TEMPLATES.put(Boolean.class, TEMPLATES.get(boolean.class));
        TEMPLATES.put(Character.class, TEMPLATES.get(char.class));
    }

    /**
     * Generates a proxy instance for the given class that implements serialization.
     *
     * @param <T>   the type of the class
     * @param clazz the class to generate a proxy for
     * @return an instance of the generated proxy
     * @throws SchemaGenerationException if generation fails
     */
    public static <T extends ByteSerializable> T generate(Class<T> clazz) {
        try {
            Object generated = GENERATED.get(clazz);

            if (generated == null) {
                validateInputClass(clazz);
                String proxyQualifiedName = generateProxyClassName(clazz);
                String sourceCode = generateSourceCode(clazz, proxyQualifiedName);
                byte[] classBytes = compileSource(proxyQualifiedName, sourceCode);

                GENERATED.put(clazz, generated = instantiateProxy(classBytes, proxyQualifiedName));

                return clazz.cast(generated);
            }
            return clazz.cast(generated.getClass().getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new SchemaGenerationException("Failed to generate proxy for " + clazz.getName(), e);
        }
    }

    private static void validateInputClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        if (!ByteSerializable.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class must implement Bytable");
        }
    }

    private static String generateProxyClassName(Class<?> clazz) {
        return clazz.getPackageName() + "." + clazz.getSimpleName() + "Proxy";
    }

    private static <T> String generateSourceCode(Class<T> clazz, String proxyQualifiedName) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        StringBuilder source = new StringBuilder();
        String packageName = clazz.getPackageName();
        String proxySimpleName = proxyQualifiedName.substring(packageName.length() + 1);

        // Header
        source.append("package ").append(packageName).append(";\n\n")
                .append("public class ").append(proxySimpleName)
                .append(" extends ").append(clazz.getName().replace('$', '.')).append(" {\n\n")
                .append("\tprivate final java.nio.ByteBuffer buffer;\n\n")
                .append("\tpublic ").append(proxySimpleName).append("() {\n")
                .append("\t\tsuper();\n")
                .append("\t\tthis.buffer = java.nio.ByteBuffer.allocate(%d);\n")
                .append("\t}\n\n");

        // Generate setters
        int offset = 0;
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            String name = field.getName();

            field.setAccessible(true);
            String setterName = toSetterName(name);
            if (!ReflectionUtility.hasMethod(clazz, setterName, type)) {
                System.err.printf("No setter %s exists in %s for field %s%n", setterName, clazz.getSimpleName(), name);
                continue;
            }

            Template template = resolveTemplate(type, field.get(instance));
            if (template == null) {
                continue;
            }

            SchemaLength length = field.getAnnotation(SchemaLength.class);
            int fieldSize = length != null ? length.max() : template.size;

            source.append("\t@Override\n")
                    .append("\tpublic void ").append(setterName).append("(")
                    .append(field.getType().getSimpleName()).append(" ").append(name).append(") {\n")
                    .append("\t\tsuper.").append(setterName).append("(").append(name).append(");\n")
                    .append("\t\t").append(String.format(template.codeSetTemplate(), offset, name)).append("\n")
                    .append("\t}\n\n");

            /* TODO source.append("""
                    @Override
                    public %s get%s() {
                    
                        return %s;
                    }
                    
                    """.formatted(type.getSimpleName(), toGetterName(field.getName())));
            */

            offset += fieldSize;
        }

        // Data method
        source.append("\t@Override\n\tpublic byte[] data() {\n\t\treturn buffer.array();\n\t}\n\n");

        // Close class
        source.append("}\n");

        // Set buffer size in constructor
        return String.format(source.toString(), offset);
    }

    private static Template resolveTemplate(Class<?> type, Object value) {
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Template base = TEMPLATES.get(componentType);
            if (base == null)
                return null;

            int arrayLen = Array.getLength(value);
            if (arrayLen <= 0)
                return null;

            String elemTpl = base.codeSetTemplate()
                    .replaceFirst("%s", "%d + i * " + base.size())
                    .replaceFirst("%s", "%s[i]");
            String looped = "for (int i = 0; i < " + arrayLen + "; i++) {\n\t\t\t" + elemTpl + "\n\t\t}";
            return new Template(looped, "", base.size * arrayLen);
        }
        return TEMPLATES.get(type);
    }

    private static byte[] compileSource(String className, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new SchemaGenerationException("No Java compiler available");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        MemoryFileManager fileManager = new MemoryFileManager(compiler.getStandardFileManager(diagnostics, null, null));
        JavaFileObject sourceFile = new StringJavaFileObject(className, sourceCode);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(sourceFile));
        if (!task.call()) {
            String error = diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new SchemaGenerationException("Compilation failed:\n" + error);
        }

        return fileManager.getClassBytes().get(className);
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateProxy(byte[] classBytes, String className)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        InMemoryClassLoader loader = new InMemoryClassLoader(Map.of(className, classBytes));
        Class<?> proxyClass = loader.loadClass(className);
        Constructor<?> constructor = proxyClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }

    private static String toSetterName(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static String toGetterName(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    // Inner classes for compilation (unchanged for brevity, but can be enhanced)
    static class StringJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        StringJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class MemoryJavaClassObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MemoryJavaClassObject(String binaryName, Kind kind) {
            super(URI.create("mem:///" + binaryName.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }

        byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, MemoryJavaClassObject> classes = new HashMap<>();

        MemoryFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            MemoryJavaClassObject classObject = new MemoryJavaClassObject(className, kind);
            classes.put(className, classObject);
            return classObject;
        }

        Map<String, byte[]> getClassBytes() {
            Map<String, byte[]> result = new HashMap<>();
            classes.forEach((name, classObject) -> result.put(name, classObject.getBytes()));
            return result;
        }
    }

    static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;

        InMemoryClassLoader(Map<String, byte[]> classBytes) {
            super(Schema.class.getClassLoader());
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }
    }

    /**
     * Custom exception for schema generation failures.
     */
    public static class SchemaGenerationException extends RuntimeException {
        public SchemaGenerationException(String message) {
            super(message);
        }

        public SchemaGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}