package de.lamsal.esrepo.api

import khttp.responses.Response

class HttpRequester : IHttpRequester {

    private fun Response.getText(): String? = this.text.takeIf { it.isNotEmpty() }

    override fun get(url: String): String? = khttp.get(url).getText()

    override fun post(url: String, data: Any): String? = khttp.post(url, data=data, headers = mapOf("Content-Type" to "application/json")).getText()

    override fun put(url: String, data: Any): String? = khttp.put(url, data=data, headers = mapOf("Content-Type" to "application/json")).getText()
}