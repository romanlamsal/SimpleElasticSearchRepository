package de.lamsal.esrepo.dsl

class Query internal constructor(
    initQuery: () -> QueryElement
) {
    private val query: QueryElement = initQuery()

    override fun toString(): String = """{"query":$query}"""
}

val query = ::Query