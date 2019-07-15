package de.lamsal.esrepo.response

data class GetResponse<T>(
    val _source: T,
    val _id: String? = null
)