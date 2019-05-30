package de.lamsal.esrepo.response

data class GetResponse<T>(
    val _source: T
)