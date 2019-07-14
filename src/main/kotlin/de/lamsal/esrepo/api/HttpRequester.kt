package de.lamsal.esrepo.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import de.lamsal.esrepo.exception.HttpError
import de.lamsal.esrepo.util.DefaultObjectMapper

class HttpRequester : IHttpRequester {
    private val mapper: ObjectMapper = DefaultObjectMapper()

    override fun get(url: String): String = url.httpGet().responseString().third.get()

    override fun post(url: String, data: Any?): String = url
        .httpPost()
        .header("Content-Type" to "application/json")
        .body(when(data) {
            is String -> data
            else -> mapper.writeValueAsString(data)
        })
        .responseString().third.get()

    override fun put(url: String, data: Any): String = url
        .httpPut()
        .header("Content-Type" to "application/json")
        .body(when(data) {
            is String -> data
            else -> mapper.writeValueAsString(data)
        })
        .responseString().third.get()
}