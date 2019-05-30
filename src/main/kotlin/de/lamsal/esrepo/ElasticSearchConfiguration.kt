package de.lamsal.esrepo

data class ElasticSearchConfiguration (
    val host: String,
    val port: Int,
    val protocol: String = "http"
)
