package de.lamsal.esrepo.api

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

internal class HttpRequesterTestIT {
    private companion object {
        const val host = "localhost"
        const val port = 6464

        private val server = ClientAndServer.startClientAndServer(port)

        @AfterAll
        @JvmStatic
        fun teardown() = server.close()
    }

    @BeforeEach
    fun beforeEach() {
        server.reset()
    }


    @Test
    fun `can successfully POST via khttp`() {
        // given
        val postBody = "foo"
        val postResponse = "bar"
        server.`when`(
            HttpRequest.request().withMethod("POST")
                .withHeader("Content-Type", "application/json")
                .withBody(postBody)
        ).respond(
            HttpResponse.response().withBody(postResponse)
        )

        // when
        val response = HttpRequester().post("http://$host:$port", postBody)

        // then
        assertEquals(postResponse, response)
    }

    @Test
    fun `can successfully PUT via khttp`() {
        // given
        val putBody = "foo"
        val putResponse = "bar"
        server.`when`(
            HttpRequest.request().withMethod("PUT")
                .withHeader("Content-Type", "application/json")
                .withBody(putBody)
        ).respond(
            HttpResponse.response().withBody(putResponse)
        )

        // when
        val response = HttpRequester().put("http://$host:$port", putBody)

        // then
        assertEquals(putResponse, response)
    }

    @Test
    fun `can successfully GET via khttp`() {
        // given
        val getResponse = "foobar"
        server.`when`(
            HttpRequest.request().withMethod("GET")
        ).respond(
            HttpResponse.response().withBody(getResponse)
        )

        // when
        val response = HttpRequester().get("http://$host:$port")

        // then
        assertEquals(getResponse, response)
    }

    @Test
    fun `should return null, when erroneous status code is given`() {
        // given
        server.`when`(
            HttpRequest.request().withMethod("GET")
        ).respond(
            HttpResponse.response().withStatusCode(404)
        )

        // when
        val response = HttpRequester().get("http://$host:$port")

        // then
        assertNull(response)
    }
}