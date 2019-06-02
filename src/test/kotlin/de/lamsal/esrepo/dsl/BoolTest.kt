package de.lamsal.esrepo.dsl

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class BoolTest {
    private val bool = bool {
        must {
            + term { "foo" to "bar" }
        }

        should {
            + term { "bar" to "baz" }
        }

        mustNot {
            + terms { "key" to listOf("foo", "bar", "baz") }
        }
    }

    @Test
    fun `should be serializable by toString`() {
        bool.toString() shouldEqual """{
            "bool": {
                "must": [ { "term": { "foo": "bar" } } ],
                "should": [ { "term": { "bar": "baz" } } ],
                "must_not": [ { "terms": { "key": ["foo", "bar", "baz"] } } ]
            }
        }""".replace("\\s".toRegex(), "")
    }

    @Test
    fun `should be serializable with only one booltype`() {
        bool {
            must {
                + term { "foo" to "bar" }
            }
        }.toString() shouldEqual """{"bool":{"must":[{"term":{"foo":"bar"}}]}}"""
    }
}