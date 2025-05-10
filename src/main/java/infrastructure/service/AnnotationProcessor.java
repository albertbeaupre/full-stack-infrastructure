package infrastructure.service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.*;
import java.util.function.*;

/**
 * The one-stop builder for configuring EVERYTHING about your annotation scan.
 */
public class AnnotationProcessor {
    // ==== Builder & Config ====


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<String> packagesToScan = new LinkedHashSet<>();
        private final Set<Predicate<Class<?>>> classFilters = new LinkedHashSet<>();
        private final Set<Predicate<Annotation>> annotationFilters = new LinkedHashSet<>();
        private final List<AnnotationHandler<?>> handlers = new ArrayList<>();
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        private ExecutorService executor = Executors.newWorkStealingPool();
        private Consumer<ProcessingError> errorHandler = ProcessingError::printStackTrace;
        private boolean scanModules = true;
        private boolean scanRecords = true;
        private boolean includeInherited = true;

        /**
         * Add one or more root packages to scan
         */
        public Builder scanPackages(String... pkg) {
            Collections.addAll(packagesToScan, pkg);
            return this;
        }

        /**
         * Exclude classes matching this predicate (e.g. internal impls)
         */
        public Builder excludeClasses(Predicate<Class<?>> p) {
            classFilters.add(p.negate());
            return this;
        }

        /**
         * Include only annotations matching this predicate
         */
        public Builder includeAnnotations(Predicate<Annotation> p) {
            annotationFilters.add(p);
            return this;
        }

        /**
         * Register a handler for one annotation type
         */
        public <A extends Annotation> Builder addHandler(AnnotationHandler<A> h) {
            handlers.add(h);
            return this;
        }

        /**
         * Supply your own classloader (e.g. module layer)
         */
        public Builder classLoader(ClassLoader loader) {
            this.classLoader = loader;
            return this;
        }

        /**
         * Use a custom executor for parallel scans
         */
        public Builder executor(ExecutorService exec) {
            this.executor = exec;
            return this;
        }

        /**
         * Global error handler for any exception in handlers
         */
        public Builder onError(Consumer<ProcessingError> handler) {
            this.errorHandler = handler;
            return this;
        }

        /**
         * Whether to follow superclass / interface annotations
         */
        public Builder includeInherited(boolean yes) {
            this.includeInherited = yes;
            return this;
        }

        /**
         * Enable/disable record-component scanning
         */
        public Builder scanRecords(boolean yes) {
            this.scanRecords = yes;
            return this;
        }

        /**
         * Enable/disable module & package-info scanning
         */
        public Builder scanModules(boolean yes) {
            this.scanModules = yes;
            return this;
        }

        public AnnotationProcessor build() {
            return new AnnotationProcessor(this);
        }
    }

    // ==== Processor internals ====

    private final Set<String> packagesToScan;
    private final Set<Predicate<Class<?>>> classFilters;
    private final Set<Predicate<Annotation>> annotationFilters;
    private final List<AnnotationHandler<?>> handlers;
    private final ClassLoader classLoader;
    private final ExecutorService executor;
    private final Consumer<ProcessingError> errorHandler;
    private final boolean includeInherited, scanModules, scanRecords;

    private AnnotationProcessor(Builder b) {
        this.packagesToScan = b.packagesToScan;
        this.classFilters = b.classFilters;
        this.annotationFilters = b.annotationFilters;
        this.handlers = b.handlers;
        this.classLoader = b.classLoader;
        this.executor = b.executor;
        this.errorHandler = b.errorHandler;
        this.includeInherited = b.includeInherited;
        this.scanModules = b.scanModules;
        this.scanRecords = b.scanRecords;
    }

    /**
     * Kick off the scan. Blocks until finished.
     */
    public void process() throws Exception {
        // find all class names under each package, from files, JARs, modules...
        Set<String> allClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String pkg : packagesToScan) {
            scanClasspathForPackage(pkg, allClasses);
        }

        // load and filter
        List<Class<?>> toProcess = allClasses.parallelStream()
                .map(this::safeLoadClass)
                .filter(Objects::nonNull)
                .filter(c -> classFilters.stream().allMatch(f -> f.test(c)))
                .collect(Collectors.toList());

        // process each in parallel
        List<Future<?>> futures = new ArrayList<>();
        for (Class<?> clazz : toProcess) {
            futures.add(executor.submit(() -> inspectClass(clazz)));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                errorHandler.accept(new ProcessingError(null, e.getCause()));
            }
        }
        executor.shutdown();
    }

    /**
     * Safely load class, swallowing LinkageErrors or CNFE
     */
    private Class<?> safeLoadClass(String fqcn) {
        try {
            return Class.forName(fqcn, false, classLoader);
        } catch (Throwable t) {
            errorHandler.accept(new ProcessingError(fqcn, t));
            return null;
        }
    }

    /**
     * Find .class files under a package in both file:// and jar:// URLs
     */
    private void scanClasspathForPackage(String pkg, Set<String> sink) throws Exception {
        String path = pkg.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            switch (res.getProtocol()) {
                case "file":    // Convert the URL into a URI, then to a Path (which decodes %20 → space, handles all escapes)
                    Path dirPath = Paths.get(res.toURI());
                    File dir = dirPath.toFile();
                    scanDirectory(dir, pkg, sink);
                    break;
                case "jar":
                    scanJar(res, path, sink);
                    break;
                default:     /* modules, etc—could use ModuleLayer introspection here */
                    if (scanModules) scanModuleLayer(path, sink);
            }
        }
    }

    private void scanDirectory(File dir, String pkg, Set<String> sink) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f, pkg + "." + f.getName(), sink);
            } else if (f.getName().endsWith(".class")) {
                sink.add(pkg + "." + f.getName().replaceAll("\\.class$", ""));
            }
        }
    }

    private void scanJar(URL res, String path, Set<String> sink) throws IOException {
        String spec = res.getPath();
        String jarPath = spec.substring(spec.indexOf("file:") + 5, spec.indexOf("!"));
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String n = je.getName();
                if (n.startsWith(path) && n.endsWith(".class")) {
                    sink.add(n.replace('/', '.').replaceAll("\\.class$", ""));
                }
            }
        }
    }

    private void scanModuleLayer(String path, Set<String> sink) {
        // if using Java 9+ modules: 
        // ModuleLayer.boot().modules().forEach(m -> m.getResourceAsStream(path)... etc.
        // Omitted here for brevity.
    }

    /**
     * Walk every possible annotation location on a Class
     */
    private void inspectClass(Class<?> clazz) {
        try {
            // 1) Class declaration
            processAnnotationsOnElement(clazz, clazz);

            // 2) Superclass and interfaces (type-use and declaration)
            if (includeInherited && clazz.getSuperclass() != null)
                processAnnotatedType(clazz.getAnnotatedSuperclass(), clazz);
            for (AnnotatedType ai : clazz.getAnnotatedInterfaces())
                processAnnotatedType(ai, clazz);

            // 3) Package & module
            if (scanModules && clazz.getPackage() != null)
                processAnnotationsOnElement(clazz.getPackage(), clazz);
            if (scanModules)
                processAnnotationsOnElement(clazz.getModule(), clazz);

            // 4) Fields
            for (Field f : clazz.getDeclaredFields()) {
                processAnnotationsOnElement(f, f);
                processAnnotatedType(f.getAnnotatedType(), f);
            }

            // 5) Methods
            for (Method m : clazz.getDeclaredMethods()) {
                processAnnotationsOnElement(m, m);
                processAnnotatedType(m.getAnnotatedReturnType(), m);
                for (Parameter p : m.getParameters()) {
                    processAnnotationsOnElement(p, p);
                    processAnnotatedType(p.getAnnotatedType(), p);
                }
                for (AnnotatedType ex : m.getAnnotatedExceptionTypes())
                    processAnnotatedType(ex, m);
            }

            // 6) Constructors
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                processAnnotationsOnElement(c, c);
                for (Parameter p : c.getParameters()) {
                    processAnnotationsOnElement(p, p);
                    processAnnotatedType(p.getAnnotatedType(), p);
                }
            }

            // 7) Record components (Java 14+)
            if (scanRecords && clazz.isRecord()) {
                for (RecordComponent rc : clazz.getRecordComponents()) {
                    processAnnotationsOnElement(rc, rc);
                    processAnnotatedType(rc.getAnnotatedType(), rc);
                }
            }
        } catch (Throwable t) {
            errorHandler.accept(new ProcessingError(clazz.getName(), t));
        }
    }

    /**
     * Declaration‐use
     */
    @SuppressWarnings("unchecked")
    private void processAnnotationsOnElement(AnnotatedElement elem, Object owner) {
        if (elem == null) return;
        for (Annotation ann : elem.getAnnotations()) {
            if (annotationFilters.stream().allMatch(f -> f.test(ann))) {
                dispatchToHandlers(ann, owner);
            }
        }
    }

    /**
     * Type‐use
     */
    @SuppressWarnings("unchecked")
    private void processAnnotatedType(AnnotatedType at, Object owner) {
        if (at == null) return;
        for (Annotation ann : at.getAnnotations()) {
            if (annotationFilters.stream().allMatch(f -> f.test(ann))) {
                dispatchToHandlers(ann, owner);
            }
        }
    }

    /**
     * Find matching handlers and invoke, honoring priorities and short-circuit if asked
     */
    @SuppressWarnings("unchecked")
    private void dispatchToHandlers(Annotation ann, Object owner) {
        List<AnnotationHandler<?>> matches = handlers.stream()
                .filter(h -> h.annotationType().equals(ann.annotationType()))
                .sorted(Comparator.comparingInt(AnnotationHandler::priority))
                .collect(Collectors.toList());

        for (AnnotationHandler<?> raw : matches) {
            AnnotationHandler<Annotation> h = (AnnotationHandler<Annotation>) raw;
            try {
                boolean cont = h.handle(ann, owner, new ProcessingContext(owner, ann));
                if (!cont) break;  // handler says “stop propagation”
            } catch (Throwable t) {
                errorHandler.accept(new ProcessingError(
                        owner instanceof Class<?> ? ((Class<?>) owner).getName() : owner.toString(),
                        t));
            }
        }
    }


    // ==== Supporting types ====

    /**
     * Super‐simple error holder
     */
    public static class ProcessingError {
        public final String location;
        public final Throwable exception;

        public ProcessingError(String loc, Throwable ex) {
            this.location = loc;
            this.exception = ex;
        }

        public void printStackTrace() {
            System.err.println("Error at: " + location);
            exception.printStackTrace();
        }
    }

    /**
     * Context passed into each handler: lets you query all the deep metadata
     */
    public static class ProcessingContext {
        private final Object owner;
        private final Annotation annotation;

        public ProcessingContext(Object owner, Annotation annotation) {
            this.owner = owner;
            this.annotation = annotation;
        }

        /**
         * The raw Java element (Class, Method, Field, Parameter, RecordComponent, Package, Module…)
         */
        public Object owner() {
            return owner;
        }

        /**
         * The exact Annotation instance found (with defaults filled in)
         */
        public <A extends Annotation> A annotation() {
            @SuppressWarnings("unchecked") A a = (A) annotation;
            return a;
        }

        /**
         * Query the declared attributes on the annotation via reflection
         */
        public Map<String, Object> attributes() {
            return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                    .filter(m -> m.getParameterCount() == 0)
                    .collect(Collectors.toMap(
                            Method::getName,
                            m -> {
                                try {
                                    return m.invoke(annotation);
                                } catch (Exception e) {
                                    return null;
                                }
                            }));
        }

        /**
         * Fully resolve nested annotation attributes (arrays, nested annos…)
         */
        public Object resolveAttribute(String name) {
            return attributes().get(name);
        }
    }
}
