package com.patternmatch.micronaut

import io.micronaut.http.annotation.*

@Controller("/ping")
class PingController {
    companion object {
        val L = logger()
    }

    @Get("/")
    fun index(): String {
        return "{\"pong\":true}"
    }
}