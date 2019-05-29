package de.lamsal.esrepo.api

class HttpRequester : IHttpRequester {

    override fun post(url: String, data: Any): String = khttp.post(url, data=data).text

    override fun put(url: String, data: Any): String = khttp.put(url, data=data).text
}