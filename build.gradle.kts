plugins {
    java
    idea
    id("me.champeau.jmh") version "0.7.2"
    id("maven-publish")
}

java {
    // Optionally keep toolchain for local development, but ensure it matches the CI environment
    // Commenting out for now to use the JDK from actions/setup-java
    // toolchain {
    //     languageVersion.set(JavaLanguageVersion.of(22))
    // }
}

idea {
    module {
        isDownloadSources = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "--release", "22"))
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

group = "backend"
version = "1.0"
val gdxVersion = "1.13.1"
val ph_cssVersion = "7.0.4"
val netty_version = "4.1.108.Final"
val jmh_version = "1.37"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "full-stack-infrastructure"
            version = version.toString()
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/AlbertBeaupre/full-stack-infrastructure")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "default-actor"
                password = System.getenv("GITHUB_TOKEN") ?: "default-token"
            }
        }
    }
}