package com.patternmatch.micronaut

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("com.patternmatch.micronaut")
                .mainClass(Application.javaClass)
                .start()
    }
}