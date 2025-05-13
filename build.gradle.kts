plugins {
    java
    idea
    id("me.champeau.jmh") version "0.7.2"
    id("maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

idea {
    module {
        isDownloadSources = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "--release", "23"))
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

group = "io.github.albertbeaupre"
version = "1.0"
val gdxVersion = "1.13.1"
val ph_cssVersion = "7.0.4"
val netty_version = "4.1.108.Final"
val jmh_version = "1.37"
var closure_compiler_version = "v20250407"
var html_compressor_version = "1.5.2"
var fast_json_version = "2.0.57"
var slf4j_version = "2.0.17"

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
    implementation("com.alibaba:fastjson:$fast_json_version")
    implementation("org.openjdk.jmh:jmh-core:$jmh_version")
    implementation("com.google.javascript:closure-compiler:$closure_compiler_version")
    implementation("com.googlecode.htmlcompressor:htmlcompressor:$html_compressor_version")
    implementation("org.slf4j:slf4j-api:$slf4j_version")
    implementation("org.slf4j:slf4j-simple:$slf4j_version")
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
            url = uri("https://maven.pkg.github.com/albertBeaupre/full-stack-infrastructure")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "default-actor"
                password = System.getenv("GITHUB_TOKEN") ?: "default-token"
            }
        }
    }
}