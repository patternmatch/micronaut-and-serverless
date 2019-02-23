import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

application {
    mainClassName = "com.patternmatch.micronaut.Application"
    applicationDefaultJvmArgs = listOf("")
}

dependencies {
    compile("io.micronaut.aws:micronaut-function-aws-api-proxy")
    compile("io.micronaut:micronaut-http-client")
    compile("io.micronaut:micronaut-http-server-netty")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("micronaut-lambda-proxy")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
}
