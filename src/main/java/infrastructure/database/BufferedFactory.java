package infrastructure.database;

import javax.tools.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class BufferedFactory {

    public static <T> T makeBufferedInstance(Class<T> cls, ObjectSchema<T> schema) throws Exception {
        String pkg = cls.getPackageName();
        String simple = cls.getSimpleName();
        String proxyName = simple + "Buffered";

        // 1) Reflectively gather all setters
        List<Method> setters = Arrays.stream(cls.getMethods()).filter(m -> m.getName().startsWith("set")).filter(m -> m.getReturnType() == void.class).filter(m -> m.getParameterCount() == 1).toList();

        // 2) Generate source code
        StringBuilder src = new StringBuilder();
        src.append("package ").append(pkg).append(";\n\n");
        src.append("public class ").append(proxyName).append(" extends ").append(simple).append(" {\n\n");
        src.append("  private final java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(").append(schema.totalSize()).append(");\n");
        src.append("  private final ").append(ObjectSchema.class.getCanonicalName()).append("<").append(simple).append("> schema;\n\n");
        // constructor
        src.append("  public ").append(proxyName).append("(").append(ObjectSchema.class.getCanonicalName()).append("<").append(simple).append("> schema) {\n").append("    super();\n").append("    this.schema = schema;\n").append("  }\n\n");
        // override each setter
        for (Method m : setters) {
            String name = m.getName();
            String paramType = m.getParameterTypes()[0].getCanonicalName();
            String prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            src.append("  @Override\n");
            src.append("  public void ").append(name).append("(").append(paramType).append(" v) {\n").append("    super.").append(name).append("(v);\n").append("    int idx = schema.indexOf(\"").append(prop).append("\");\n").append("    schema.mappers().get(idx).write(buf, 0, this);\n").append("  }\n\n");
        }
        // expose bytes
        src.append("  public byte[] getBytes() { return buf.array(); }\n");
        src.append("}\n");

        // 3) Compile in-memory
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///" + pkg.replace('.', '/') + "/" + proxyName + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignore) {
                return src.toString();
            }
        };

        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null)) {
            JavaCompiler.CompilationTask task = compiler.getTask(null, fm, null, null, null, Collections.singletonList(file));
            if (!task.call()) {
                throw new RuntimeException("Compilation failed:\n" + src);
            }

            // 4) Load the compiled class
            ClassLoader cl = fm.getClassLoader(null);
            @SuppressWarnings("unchecked") Class<? extends T> proxyCls = (Class<? extends T>) cl.loadClass(pkg + "." + proxyName);

            // 5) Instantiate and return
            return proxyCls.getConstructor(ObjectSchema.class).newInstance(schema);
        }
    }
}
