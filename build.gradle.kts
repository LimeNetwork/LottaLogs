import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
    id("io.freefair.lombok") version "9.2.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.crazycrew.us/releases")
    }
}

dependencies {
    implementation(libs.org.reflections.reflections)
    implementation(libs.com.squareup.okhttp3.okhttp)
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.me.filoghost.farmlimiter.farmlimiter.api)
    compileOnly(libs.com.github.griefprevention)
}

group = "com.daki.lottalogs"
version = "1.0"

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    minimize();
    relocate("com.squareup.okhttp3", "com.daki.lottalogs.libs.okhttp3")
    relocate("org.reflections", "com.daki.lottalogs.libs.reflections")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}