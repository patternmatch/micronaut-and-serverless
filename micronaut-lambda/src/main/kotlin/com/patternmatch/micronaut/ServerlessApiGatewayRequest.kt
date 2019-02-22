package com.patternmatch.micronaut

data class ServerlessApiGatewayRequest(var body: FunctionRequest,
                                       var method: String,
                                       var principalId: String,
                                       var stage: String)