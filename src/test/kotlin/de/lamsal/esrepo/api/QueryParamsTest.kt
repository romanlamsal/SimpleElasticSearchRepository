package de.lamsal.esrepo.api

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class QueryParamsTest {

    @Test
    fun `should correctly serialize to string with params set`() {
        // given
        val params = QueryParams(size = 1337, scroll = "42m")

        // when, then
        params.toString() shouldEqual "?scroll=42m&size=1337"
    }

    @Test
    fun `should correctly serialize to string with only one params set and the other being null`() {
        // given
        val params = QueryParams(size = 1337, scroll = null)

        // when, then
        params.toString() shouldEqual "?size=1337"
    }

    @Test
    fun `should serialize to empty string with no params set`() {
        // given
        val params = QueryParams()

        // when, then
        params.toString() shouldEqual ""
    }
}