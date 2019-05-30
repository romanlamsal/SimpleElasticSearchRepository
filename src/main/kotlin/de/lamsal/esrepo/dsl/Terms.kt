package de.lamsal.esrepo.dsl

class Terms (
    initTerm: () -> Pair<String, List<Any>>
) : QueryElement {
    private val terms = initTerm().let { mapOf(it.first to it.second) }

    override fun toString(): String = terms.entries.first().run {
        """{"terms":{"$key":${value.joinToString(prefix = "[", separator = ",", postfix = "]") {
            when(it) {
                is String -> "\"$it\""
                else -> it.toString()
            }
        }}}}"""
    }
}