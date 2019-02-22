package com.patternmatch.micronaut

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.services.lambda.runtime.*
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import java.io.*

class StreamLambdaHandler : RequestStreamHandler {

    companion object {
        lateinit var handler: MicronautLambdaContainerHandler
    }

    init {
        try {
            handler = MicronautLambdaContainerHandler.getAwsProxyHandler()
        } catch (exception: ContainerInitializationException) {
            // if we fail here. We re-throw the exception to force another cold start
            exception.printStackTrace()
            throw RuntimeException("Could not initialize Micronaut", exception)
        }
    }

    override fun handleRequest(input: InputStream?, output: OutputStream?, context: Context?) {
        handler.proxyStream(input, output, context)
    }
}