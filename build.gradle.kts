plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "--release", "22"))  // Enable preview for compilation
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")  // Enable preview for test execution
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")  // Enable preview for runtime execution
}


group = "backend"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.netty:netty-all:4.1.108.Final")
    implementation("com.displee:rs-cache-library:6.9-RC2")
}

tasks.test {
    useJUnitPlatform()
}