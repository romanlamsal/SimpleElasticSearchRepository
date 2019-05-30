package de.lamsal.esrepo.dsl

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class QueryTest {
    private val query = Query {
            Term {
                "foo" to "bar"
            }
        }

    private val jsonString = """{"query":{"term":{"foo":"bar"}}}"""

    @Test
    fun `should be serializable by toString with term`() {
        query.toString() shouldEqual jsonString
    }
}