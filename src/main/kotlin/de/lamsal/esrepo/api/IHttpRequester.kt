package de.lamsal.esrepo.api

// return 'null' on error
interface IHttpRequester {
    fun post(url: String, data: Any? = null): String
    fun put(url: String, data: Any): String
    fun get(url: String): String
}