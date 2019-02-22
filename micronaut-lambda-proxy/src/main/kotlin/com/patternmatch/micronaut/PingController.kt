package com.patternmatch.micronaut

import io.micronaut.http.annotation.*

@Controller("/ping")
class PingController {

    @Get("/")
    fun index(): String {
        return "{\"pong\":true}"
    }
}