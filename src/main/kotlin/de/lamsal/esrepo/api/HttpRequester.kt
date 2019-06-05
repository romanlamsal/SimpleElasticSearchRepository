package de.lamsal.esrepo.api

import de.lamsal.esrepo.exception.HttpError
import khttp.responses.Response
import java.lang.Exception

class HttpRequester : IHttpRequester {

    private fun Response.isOkay(): Boolean = statusCode in 200..299

    private fun Response.getText(): String = this.text.takeIf { this.isOkay() } ?: throw HttpError(Exception("Status code $statusCode: $text"))

    override fun get(url: String): String = khttp.get(url).getText()

    override fun post(url: String, data: Any?): String = khttp.post(url, data=data, headers = mapOf("Content-Type" to "application/json")).getText()

    override fun put(url: String, data: Any): String = khttp.put(url, data=data, headers = mapOf("Content-Type" to "application/json")).getText()
}