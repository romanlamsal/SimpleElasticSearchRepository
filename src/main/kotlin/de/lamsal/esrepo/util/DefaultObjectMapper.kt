package de.lamsal.esrepo.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class DefaultObjectMapper : ObjectMapper() {
    init {
        registerModule(KotlinModule())
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}