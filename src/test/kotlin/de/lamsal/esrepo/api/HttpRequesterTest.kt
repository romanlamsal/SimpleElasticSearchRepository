package de.lamsal.esrepo.api

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

internal class HttpRequesterTestIT {
    private companion object {
        const val host = "localhost"
        const val port = 6464
    }

    @Test
    fun `can successfully POST via khttp`() {
        ClientAndServer.startClientAndServer(port).use {
            // given
            val postBody = "foo"
            val postResponse = "bar"
            it.`when`(
                HttpRequest.request().withMethod("POST").withBody(postBody)
            ).respond(
                HttpResponse.response().withBody(postResponse)
            )

            // when
            val response = HttpRequester().post("http://$host:$port", postBody)

            // then
            response shouldEqual postResponse
        }
    }

    @Test
    fun `can successfully PUT via khttp`() {
        ClientAndServer.startClientAndServer(port).use {
            // given
            val putBody = "foo"
            val putResponse = "bar"
            it.`when`(
                HttpRequest.request().withMethod("PUT").withBody(putBody)
            ).respond(
                HttpResponse.response().withBody(putResponse)
            )

            // when
            val response = HttpRequester().put("http://$host:$port", putBody)

            // then
            response shouldEqual putResponse
        }
    }
}