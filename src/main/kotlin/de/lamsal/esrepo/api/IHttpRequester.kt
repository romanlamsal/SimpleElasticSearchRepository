package de.lamsal.esrepo.api

interface IHttpRequester {
    fun post(url: String, data: Any): String
    fun put(url: String, data: Any): String
}