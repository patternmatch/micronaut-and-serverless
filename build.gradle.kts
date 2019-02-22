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
}
