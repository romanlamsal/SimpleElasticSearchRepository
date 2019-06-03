package de.lamsal.esrepo.api

import kotlin.reflect.full.memberProperties

data class QueryParams(val size: Int? = null, val scroll: String? = null) {
    override fun toString(): String = QueryParams::class.memberProperties
        .filter { it.get(this) != null }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(prefix = "?", separator = "&") {
            "${it.name}=${it.get(this)}"
        }
        ?: ""
}