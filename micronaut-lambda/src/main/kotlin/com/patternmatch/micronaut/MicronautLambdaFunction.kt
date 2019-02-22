package com.patternmatch.micronaut

import io.micronaut.function.FunctionBean
import java.util.function.Function

@FunctionBean("micronaut-lambda")
class MicronautLambdaFunction : Function<ServerlessApiGatewayRequest, FunctionResponse> {
    override fun apply(request: ServerlessApiGatewayRequest) =
            FunctionResponse(request.body.someValue, request.body.message, true)
}