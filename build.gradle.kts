allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

plugins {
    id("com.adarshr.test-logger") version "1.6.0" apply false
}

subprojects {
    version = "1.0"
    apply(plugin = "com.adarshr.test-logger")
}

configure(subprojects.filter { it.name == "micronaut-lambda" || it.name == "micronaut-lambda-proxy" }) { 
    apply(plugin = "groovy")

    dependencies {
        "testCompile"("org.spockframework:spock-core:1.0-groovy-2.4") {
            exclude(module = "groovy-all")
        }
    }

    val subProjectName = name;
    val subProjectPath = projectDir.absolutePath
    val userHomeDir = System.getProperty("user.home")

    fun slsRunInDocker(slsCommand: String) =
        "docker run -v ${subProjectPath}:/workspace -v ${userHomeDir}/.aws:/home/appuser/.aws -w /workspace serverless-build-image /bin/bash -c \"${slsCommand}\""

    tasks.register<Exec>("slsDeploy") {
        dependsOn(":${subProjectName}:build")
        commandLine("bash", "-c", slsRunInDocker("sls deploy -v"))
    }

    tasks.register<Exec>("slsRemove") {
        commandLine("bash", "-c", slsRunInDocker("sls remove"))
    }
}

tasks.register<Exec>("buildDockerImageForSls") {
    commandLine("docker", "build", "-t", "serverless-build-image", ".")
}

