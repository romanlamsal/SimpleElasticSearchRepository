package de.lamsal.esrepo.dsl

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class TermsTest {
    private val terms = terms {
        "foo" to listOf("bar", "baz", 1337)
    }
    private val termJson = """{"terms":{"foo":["bar","baz",1337]}}"""

    @Test
    fun `should be serializable with toString`() {
        terms.toString() shouldBe termJson
    }
}