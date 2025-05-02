plugins {
    java
    idea
    id("me.champeau.jmh") version "0.7.2"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "io.github.albertbeaupre"
version = "0.1"

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
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

val gdxVersion = "1.13.1"
val ph_cssVersion = "7.0.4"
val netty_version = "4.1.108.Final"
val jmh_version = "1.37"
val berkeleydb_version = "18.3.12"
val sqlite_version = "3.42.0.0"
val closure_compiler_version = "v20250407"
val html_compressor_version = "1.5.2"
val fast_json_version = "2.0.57"

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
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmh_version")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // publish the “java” component
            from(components["java"])

            // keep these in sync with your group/version/artifactId
            groupId    = project.group.toString()
            artifactId = "full-stack-infrastructure"
            version    = project.version.toString()

            pom {
                name.set("full-stack-infrastructure")
                description.set("A full-stack infrastructure Java library")
                url.set("https://github.com/albertBeaupre/full-stack-infrastructure")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("albertbeaupre")
                        name.set("Albert Beaupre")
                        email.set("you@example.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/albertBeaupre/full-stack-infrastructure.git")
                    developerConnection.set("scm:git:ssh://github.com:albertBeaupre/full-stack-infrastructure.git")
                    url.set("https://github.com/albertbeaupre/full-stack-infrastructure")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatypeSnapshots"
            url  = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = findProperty("sonatypeUsername") as String?
                    ?: System.getenv("SONATYPE_USERNAME")
                password = findProperty("sonatypePassword") as String?
                    ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}



signing {
    useInMemoryPgpKeys(
        findProperty("signing.keyId") as String?,
        findProperty("signing.secretKey") as String?,
        findProperty("signing.password") as String?
    )
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    repositories {
        sonatype {
            // override the legacy host to the new, token‑friendly one
            nexusUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            // your sonatypeUsername/sonatypePassword from ~/.gradle/gradle.properties
            // will be picked up automatically, so no need to re‑declare them here
        }
    }
}



