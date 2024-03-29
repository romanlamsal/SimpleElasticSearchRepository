package de.lamsal.esrepo.api

import com.github.kittinunf.fuel.core.FuelError
import de.lamsal.esrepo.exception.HttpError
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.junit.jupiter.api.Assertions.assertThrows as assertThrows

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
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(postResponse)
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
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(putResponse)
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
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(getResponse)
        )

        // when
        val response = HttpRequester().get("http://$host:$port")

        // then
        assertEquals(getResponse, response)
    }

    @Test
    fun `should throw error, when erroneous status code is given`() {
        // given
        server.`when`(
            HttpRequest.request().withMethod("GET")
        ).respond(
            HttpResponse.response()
                .withStatusCode(404)
        )

        // when, then
        assertThrows(FuelError::class.java) {
            HttpRequester().get("http://$host:$port")
        }
    }
}