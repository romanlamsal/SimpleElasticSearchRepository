package de.lamsal.esrepo.dsl

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class TermTest {
    private val term = term { "foo" to "bar" }
    private val termJson = """{"term":{"foo":"bar"}}"""

    @Test
    fun `should be serializable with toString`() {
        term.toString() shouldEqual termJson
    }

    @Test
    fun `should be serializable with toString and a number as value`() {
        term { "foo" to 1337 }.toString() shouldEqual """{"term":{"foo":1337}}"""
    }
}