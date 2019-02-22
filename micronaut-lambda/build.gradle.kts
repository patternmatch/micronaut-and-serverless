import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project     // Get from gradle.properties
val micronautVersion: String by project  // Get from gradle.properties

plugins {
    application
    id("io.spring.dependency-management") version "1.0.6.RELEASE" // To import Maven BOM files in Gradle (for ex. Micronaut)
    id("com.github.johnrengelman.shadow") version "4.0.2"         // To build fat jars
    id("org.jetbrains.kotlin.jvm") version "1.3.21"               // Kotlin
    id("org.jetbrains.kotlin.kapt") version "1.3.21"              // Kotlin annotation processor
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.21"    // Kotlin plugin that automatically opens classes for Micronaut AOP
}

version = 1.0
group = "com.patternmatch.micronaut"

dependencyManagement {
    imports {
        mavenBom("io.micronaut:micronaut-bom:${micronautVersion}")
    }
}

application {
    mainClassName = "io.micronaut.function.executor.FunctionApplication" // Required by Micronaut and @FunctionBean
    applicationDefaultJvmArgs = listOf("")
}

dependencies {
    annotationProcessor("io.micronaut:micronaut-inject-java")
    annotationProcessor("io.micronaut:micronaut-validation")

    compile("io.micronaut:micronaut-inject")
    compile("io.micronaut:micronaut-validation")
    compile("io.micronaut:micronaut-runtime")
    compile("io.micronaut:micronaut-function-aws")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    compile("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")

    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")

    kaptTest("io.micronaut:micronaut-inject-java")

    runtime("ch.qos.logback:logback-classic:1.2.3")
    runtime("com.amazonaws:aws-lambda-java-log4j2:1.0.0")
    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")
    runtime("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")

    testAnnotationProcessor("io.micronaut:micronaut-inject-java")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("micronaut-lambda")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}

tasks.named<JavaExec>("run") {
    doFirst {
        jvmArgs = listOf("-noverify", "-XX:TieredStopAtLevel=1")
    }
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
}
