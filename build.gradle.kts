import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

plugins {
    id("com.adarshr.test-logger") version "1.6.0" apply false                   // To show running tests on the console
    id("io.spring.dependency-management") version "1.0.6.RELEASE" apply false   // To import Maven BOM files in Gradle (for ex. Micronaut)
    id("com.github.johnrengelman.shadow") version "4.0.2" apply false           // To build fat jars
    id("org.jetbrains.kotlin.jvm") version "1.3.21" apply false                 // Kotlin
    id("org.jetbrains.kotlin.kapt") version "1.3.21" apply false                // Kotlin annotation processor
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.21" apply false      // Kotlin plugin that automatically opens classes for Micronaut AOP
}

subprojects {
    version = "1.0"
    group = "com.patternmatch.micronaut"
    apply(plugin = "com.adarshr.test-logger")
}

configure(subprojects.filter { it.name == "micronaut-lambda" || it.name == "micronaut-lambda-proxy" }) {
    apply(plugin = "groovy")
    apply(plugin = "application")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

    val kotlinVersion: String by project     // Get from gradle.properties
    val micronautVersion: String by project  // Get from gradle.properties

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("io.micronaut:micronaut-bom:${micronautVersion}")
        }
    }

    dependencies {
        "compile"("io.micronaut:micronaut-inject")
        "compile"("io.micronaut:micronaut-validation")
        "compile"("io.micronaut:micronaut-runtime")
        "compile"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
        "compile"("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")

        "annotationProcessor"("io.micronaut:micronaut-inject-java")
        "annotationProcessor"("io.micronaut:micronaut-validation")

        "kapt"("io.micronaut:micronaut-inject-java")
        "kapt"("io.micronaut:micronaut-validation")

        "kaptTest"("io.micronaut:micronaut-inject-java")
        "testAnnotationProcessor"("io.micronaut:micronaut-inject-java")

        "testCompile"("org.spockframework:spock-core:1.0-groovy-2.4") {
            exclude(module = "groovy-all")
        }

        "runtime"("ch.qos.logback:logback-classic:1.2.3")
        "runtime"("com.amazonaws:aws-lambda-java-log4j2:1.0.0")
        "runtime"("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")
        "runtime"("com.fasterxml.jackson.mod" +
                "ule:jackson-module-kotlin:2.9.7")
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

    configure<AllOpenExtension> {
        annotation("io.micronaut.aop.Around")
    }

    val subProjectName = name;
    val subProjectPath = projectDir.absolutePath
    val userHomeDir = System.getProperty("user.home")

    // Run slsCommand in Docker which holds configured Serverless framework
    fun slsRunInDocker(slsCommand: String) =
            "docker run -v ${subProjectPath}:/workspace -v ${userHomeDir}/.aws:/home/appuser/.aws -w /workspace serverless-build-image /bin/bash -c \"${slsCommand}\""

    // Deploy subproject into AWS using Serverless in Docker
    tasks.register<Exec>("slsDeploy") {
        dependsOn(":${subProjectName}:build")
        commandLine("bash", "-c", slsRunInDocker("sls deploy -v"))
    }

    // Deploy specified function without any stack manipulations (much faster than slsDeploy)
    // run gradle with: ./gradlew -PfunName=riposter
    tasks.register<Exec>("slsDeployFunction") {
        val funName: String? by project
        dependsOn(":${subProjectName}:build")
        commandLine("bash", "-c", slsRunInDocker("sls deploy function -f ${funName}"))
    }

    // Remove all AWS resources related to deployment done by slsDeploy task
    tasks.register<Exec>("slsRemove") {
        commandLine("bash", "-c", slsRunInDocker("sls remove"))
    }
}

// One-time only task: will use Dockerfile and build Docker image named: serverless-build-image.
// This image can be used to run Serverless commands on artifacts from our projects.
tasks.register<Exec>("buildDockerImageForSls") {
    commandLine("docker", "build", "-t", "serverless-build-image", ".")
}
