package de.lamsal.esrepo.dsl

class Term internal constructor(
    initTerm: () -> Pair<String, Any>
) : QueryElement {
    private val term = initTerm().let { mapOf(it.first to it.second) }

    override fun toString(): String = term.entries.first().run {
        """{"term":{"$key":${
        when (value) {
            is String -> "\"$value\""
            else -> value
        }}}}"""
    }
}

val term = ::Term