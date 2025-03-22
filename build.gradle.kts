plugins {
    java
    idea
    id("me.champeau.jmh") version "0.7.2" // JMH plugin
    id("maven-publish") // Added for publishing to JitPack
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

idea {
    module {
        isDownloadSources = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "--release", "22")) // Enable preview for compilation
}

tasks.withType<Test> {
    jvmArgs("--enable-preview") // Enable preview for test execution
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview") // Enable preview for runtime execution
}

group = "full-stack" // This will be part of your artifact's groupId
version = "1.0"   // Version for your library
val gdxVersion = "1.13.1"
val ph_cssVersion = "7.0.4"
val netty_version = "4.1.108.Final"
val jmh_version = "1.37"

repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' } // Added JitPack repository (optional for local testing)
}

dependencies {
    // Existing dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.netty:netty-all:$netty_version")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.helger:ph-css:$ph_cssVersion")
    implementation("org.openjdk.jmh:jmh-core:$jmh_version")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmh_version")
}

tasks.test {
    useJUnitPlatform()
}

// Configure publishing for JitPack
publishing {
    publications {
        mavenJava(MavenPublication) {
            groupId = group.toString()           // e.g., "backend"
            artifactId = "full-stack-infrastructure"     // Replace with your desired artifact name
            version = version.toString()         // e.g., "1.0"

            from(components.java)                // Publish the Java component (your compiled code)
        }
    }
}