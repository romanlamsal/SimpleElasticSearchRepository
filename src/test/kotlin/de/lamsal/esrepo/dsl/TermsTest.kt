package de.lamsal.esrepo.dsl

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class TermsTest {
    private val terms = Terms {
        "foo" to listOf("bar", "baz", 1337)
    }
    private val termJson = """{"terms":{"foo":["bar","baz",1337]}}"""

    @Test
    fun `should be serializable with toString`() {
        terms.toString() shouldEqual termJson
    }
}