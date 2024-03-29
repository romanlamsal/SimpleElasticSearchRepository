package de.lamsal.esrepo.dsl

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class QueryTest {
    private val query = query {
            term {
                "foo" to "bar"
            }
        }

    private val jsonString = """{"query":{"term":{"foo":"bar"}}}"""

    @Test
    fun `should be serializable by toString with term`() {
        query.toString() shouldBe jsonString
    }
}