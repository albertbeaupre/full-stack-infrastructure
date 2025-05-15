package infrastructure.tools;

import infrastructure.collections.bits.ByteBits;
import infrastructure.net.web.Route;
import infrastructure.net.web.WebServer;
import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.Designer;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.components.FileUploader;
import infrastructure.net.web.ui.components.Label;
import infrastructure.net.web.ui.components.TextField;
import infrastructure.net.web.ui.css.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Year;

public final class WebServerProjectBuilder {

    private static final String JAVA_VERSION = "23";
    private static final ByteBits FLAGS = new ByteBits();
    private static final byte INCLUDE_THEME_FLAG = 0;
    private static final byte INCLUDE_TEST_FOLDER_FLAG = 1;
    private static final byte INCLUDE_EXAMPLE_ROUTE = 2;

    private static Terminal TERMINAL;
    private static TextField PROJECT_PATH_FIELD;
    private static TextField PROJECT_NAME_FIELD;
    private static TextField PACKAGE_NAME_FIELD;

    public static void main() throws Exception {
        WebServer.start(4040, true);

        WebServer.getRouter().addRoute(new Route() {
            @Override
            public void load(UI ui) {
                TERMINAL = new Terminal();
                PROJECT_PATH_FIELD = new TextField("Project Path");
                PROJECT_NAME_FIELD = new TextField("Project Name");
                PACKAGE_NAME_FIELD = new TextField("Package Name");
                FileUploader uploader = new FileUploader();
                uploader.setHandler(new FileUploader.FileUploadHandler() {
                    @Override
                    public void handle(String fileName, byte[] data) {
                        System.out.println("WTF: "+fileName);
                    }
                });

                ui.setTitle("Web Server Project Builder");

                Designer.begin(ui)
                        .h1("Web Server Project Builder")
                        .textAlign(TextAlign.CENTER)
                        .label("Uses Java Version " + JAVA_VERSION)
                        .textAlign(TextAlign.CENTER)
                        .marginBottom("2.5em")
                        .component(uploader)

                        .div()
                        .asParent()
                        .display(Display.FLEX)
                        .flexDirection(FlexDirection.COLUMN)
                        .justifyContent(JustifyContent.CENTER)
                        .alignItems(AlignItems.CENTER)
                        .gap("1em")

                        .component(PROJECT_PATH_FIELD)
                        .width("30%")
                        .minWidth("25%")
                        .maxWidth("50%")

                        .component(PROJECT_NAME_FIELD)
                        .width("30%")
                        .minWidth("25%")
                        .maxWidth("50%")

                        .component(PACKAGE_NAME_FIELD)
                        .width("30%")
                        .minWidth("25%")
                        .maxWidth("50%")

                        .div()
                        .asParent()
                        .alignSelf(AlignSelf.CENTER)

                        .div()
                        .asParent()
                        .alignSelf(AlignSelf.START)
                        .display(Display.FLEX)
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.BASELINE)
                        .gap("0.5em")
                        .checkbox()
                        .onValueChange(e -> FLAGS.set(INCLUDE_THEME_FLAG, (Boolean) e.getNewValue()))
                        .minWidth("16px")
                        .minHeight("16px")
                        .label("Include Theme")
                        .goBack()
                        .goBack()

                        .div()
                        .asParent()
                        .alignSelf(AlignSelf.START)
                        .display(Display.FLEX)
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.BASELINE)
                        .gap("0.5em")
                        .checkbox()
                        .onValueChange(e -> FLAGS.set(INCLUDE_EXAMPLE_ROUTE, (Boolean) e.getNewValue()))
                        .minWidth("16px")
                        .minHeight("16px")
                        .label("Include Example Route")
                        .goBack()
                        .goBack()

                        .div()
                        .asParent()
                        .alignSelf(AlignSelf.START)
                        .display(Display.FLEX)
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.BASELINE)
                        .gap("0.5em")
                        .checkbox()
                        .onValueChange(e -> FLAGS.set(INCLUDE_TEST_FOLDER_FLAG, (Boolean) e.getNewValue()))
                        .minWidth("16px")
                        .minHeight("16px")
                        .label("Include Test Folder")
                        .goBack()
                        .marginBottom("2em")
                        .goBack()
                        .button("Generate Project")
                        .onClick(e -> {
                            e.getComponent().setEnabled(false);
                            generateProject();
                        })
                        .goBack()
                        .goBack()

                        .component(TERMINAL)
                        .width("80%");
            }

            @Override
            public String getPath() {
                return "/";
            }
        });
    }

    static void generateProject() {
        TERMINAL.text("Welcome to Web Server Project Builder!");
        TERMINAL.text(" ");
        TERMINAL.text("Generating project...");

        String projectPath = PROJECT_PATH_FIELD.getValue();
        String projectName = PROJECT_NAME_FIELD.getValue();
        String packageName = PACKAGE_NAME_FIELD.getValue();

        Path root = Paths.get(projectPath);
        String pkgPath = packageName.replace('.', '/');

        TERMINAL.text("Generating source packages " + pkgPath + "...");
        Path mainJavaPath = root.resolve("src/main/java/" + pkgPath);
        Path mainResourcePath = root.resolve("src/main/resources");
        Path webResourcePath = mainResourcePath.resolve("web");
        Path jsResourcePath = webResourcePath.resolve("js");
        Path cssResourcePath = webResourcePath.resolve("css");
        try {
            // Create directories
            TERMINAL.text("Creating directories...");
            Files.createDirectories(mainJavaPath);
            Files.createDirectories(mainResourcePath);
            Files.createDirectories(webResourcePath);
            Files.createDirectories(jsResourcePath);
            Files.createDirectories(cssResourcePath);
            TERMINAL.text("Complete!");


            TERMINAL.text("Copying runtime.js...");
            Files.write(jsResourcePath.resolve("runtime.js"), Files.readAllBytes(Path.of("src/main/resources/web/js/runtime.js")));
            TERMINAL.text("Complete!");


            if (FLAGS.get(INCLUDE_TEST_FOLDER_FLAG)) {
                TERMINAL.text("Generating test package " + pkgPath + "...");
                Path testJava = root.resolve("src/test/java/" + pkgPath);
                Path testRes = root.resolve("src/test/resources");
                TERMINAL.text("Creating test package " + pkgPath + "...");
                Files.createDirectories(testJava);
                Files.createDirectories(testRes);
                TERMINAL.text("Complete!");
            }

            if (FLAGS.get(INCLUDE_THEME_FLAG)) {
                TERMINAL.text("Copying theme.css...");
                Files.write(cssResourcePath.resolve("theme.css"), Files.readAllBytes(Path.of("src/main/resources/web/css/theme.css")));
                TERMINAL.text("Complete!");
            }

            TERMINAL.text("Generating .gitignore...");
            generateGitignore(root);
            TERMINAL.text("Complete!");

            TERMINAL.text("Generating README...");
            generateReadme(root, projectName, packageName);
            TERMINAL.text("Complete!");

            TERMINAL.text("Generating LICENSE...");
            generateLicense(root);
            TERMINAL.text("Complete!");

            TERMINAL.text("Generating build.gradle...");
            generateGradle(root, packageName, projectName);
            TERMINAL.text("Complete!");


            if (FLAGS.get(INCLUDE_EXAMPLE_ROUTE)) {
                TERMINAL.text("Generating Application.java...");
                generateApplicationClass(mainJavaPath, packageName, projectName);
                TERMINAL.text("Complete!");
            }

            TERMINAL.text("Project generated at " + root.toAbsolutePath());
        } catch (IOException e) {
            TERMINAL.text("Error generating project: " + e.getMessage());
        }
    }

    private static void generateGitignore(Path root) throws IOException {
        String content = """
                # Compiled class files
                *.class
                
                # Build folders
                build/
                .gradle/
                
                # IDE and system files
                .idea/
                *.iml
                """;
        Files.writeString(root.resolve(".gitignore"), content);
    }

    private static void generateReadme(Path root, String projectName, String packageName) throws IOException {
        String sb = "# " + projectName + "\n\n" +
                "Generated with WebServerProjectBuilder.\n\n" +
                "## Requirements\n" +
                "- Java " + JAVA_VERSION + "\n\n" +
                "## Running\n" +
                "```\n" +
                "gradle run\n" +
                "```\n";
        Files.writeString(root.resolve("README.md"), sb);
    }

    private static void generateLicense(Path root) throws IOException {
        String year = String.valueOf(Year.now().getValue());
        String content = """
                MIT License
                
                Copyright (c) YEAR
                
                Permission is hereby granted, free of charge, to any person obtaining a copy
                of this software and associated documentation files (the "Software"), to deal
                in the Software without restriction, including without limitation the rights
                to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                copies of the Software, and to permit persons to whom the Software is
                furnished to do so, subject to the following conditions:
                
                The above copyright notice and this permission notice shall be included in all
                copies or substantial portions of the Software.
                
                THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
                SOFTWARE.
                """.replace("YEAR", year);
        Files.writeString(root.resolve("LICENSE"), content);
    }

    private static void generateGradle(Path root, String packageName, String projectName) throws IOException {
        // settings.gradle.kts
        String settings = "rootProject.name = \"" + projectName + "\"\n";
        Files.writeString(root.resolve("settings.gradle"), settings);

        // build.gradle
        String build = """
                plugins {
                    application
                    java
                }
                
                group = %s
                version = "1.0.0"
                
                java {
                    sourceCompatibility = JavaVersion.VERSION_23
                    targetCompatibility = JavaVersion.VERSION_23
                }
                
                application {
                    mainClass.set("PACKAGE.Main")
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation("infrastructure:infrastructure-net-web:1.0.0")
                }
                """.formatted(packageName);
        Files.writeString(root.resolve("build.gradle"), build);
    }

    private static void generateApplicationClass(Path mainJava, String packageName, String projectName) throws IOException {
        // 1) Ensure the package folder exists right here:
        Files.createDirectories(mainJava);

        // 2) Build the path to Main.java
        Path file = mainJava.resolve("Application.java");

        // 3) The text-block for your Main class
        String content = """
                package %1$s;
                
                import infrastructure.net.web.Route;
                import infrastructure.net.web.WebServer;
                import infrastructure.net.web.ui.UI;
                import infrastructure.net.web.ui.components.Label;
                
                public class Application {
                    public static void main(String[] args) {
                        WebServer.start(8080, true);
                        WebServer.getRouter().addRoute(new Route() {
                            @Override
                            public void load(UI ui) {
                                ui.setTitle("Hello, %2$s!");
                                ui.div().add(new Label("Welcome to %2$s"));
                            }
                
                            @Override
                            public String getPath() { return "/"; }
                        });
                    }
                }
                """.formatted(packageName, projectName);

        // 4) Write the file (create or overwrite) with UTF-8
        Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static class Terminal extends Component {

        public Terminal() {
            super("div");
        }

        @Override
        protected void create() {
            this.getStyle()
                    .scrollBehavior(ScrollBehavior.AUTO)
                    .overflow(Overflow.SCROLL)
                    .background("black")
                    .color("white")
                    .fontSize("14px")
                    .lineHeight("1.4")
                    .padding("1em")
                    .width("100%")
                    .height("400px")
                    .border("none")
                    .boxShadow("inset 0 0 5px rgba(0,255,0,0.2")
                    .whiteSpace(WhiteSpace.PRE_WRAP);
        }

        @Override
        protected void destroy() {

        }

        public void text(String text) {
            Label label = new Label(text);
            label.getStyle()
                    .textAlign(TextAlign.LEFT)
                    .color("white")
                    .fontFamily("Source Code Pro");

            this.add(label);
        }
    }

}
