import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

application {
    mainClassName = "io.micronaut.function.executor.FunctionApplication" // Required by Micronaut and @FunctionBean
    applicationDefaultJvmArgs = listOf("")
}

dependencies {
    compile("io.micronaut:micronaut-function-aws")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("micronaut-lambda")
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
