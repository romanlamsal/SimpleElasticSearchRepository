package de.lamsal.esrepo.configuration

data class ElasticSearchConfiguration (
    val host: String,
    val port: Int,
    val protocol: String = "http"
)
