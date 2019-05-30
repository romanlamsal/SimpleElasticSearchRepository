package de.lamsal.esrepo.dsl

class Query (
    initQuery: () -> QueryElement
) {
    private val query: QueryElement = initQuery()

    override fun toString(): String = """{"query":$query}"""
}