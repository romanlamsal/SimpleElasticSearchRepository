package de.lamsal.esrepo.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.api.IHttpRequester
import de.lamsal.esrepo.configuration.ElasticSearchConfiguration
import de.lamsal.esrepo.util.DefaultObjectMapper
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

internal class SimpleRepositoryTest {
    private companion object {
        const val index = "testindex"
        const val doctype = "thedoctype"
        const val expectedId = "some_id"
        const val saveResponseJson = """{
            "_shards" : {
                "total" : 2,
                "failed" : 0,
                "successful" : 2
            },
            "_index" : "$index",
            "_type" : "$doctype",
            "_id" : "$expectedId",
            "_version" : 1,
            "_seq_no" : 0,
            "_primary_term" : 1,
            "result" : "created"
        }"""


        val mapper = DefaultObjectMapper()

        val configuration = ElasticSearchConfiguration(host = "localhost", port = 9200)
    }

    private lateinit var repository: SimpleRepository<Entity>

    @BeforeEach
    fun beforeEach() {
        repository = SimpleRepository(
            configuration = configuration,
            mapper = mapper,
            index = index,
            doctype = doctype,
            api = mock<HttpRequester> {
                on {
                    post(configuration.run { "$protocol://$host:$port" } + "/$index/$doctype", any())
                } doReturn saveResponseJson

                on {
                    post(configuration.run { "$protocol://$host:$port" } + "/$index/$doctype/$expectedId", any())
                } doReturn saveResponseJson
            }
        )
    }

    @Test
    fun `should POST against ES when saving without ID, sets responss's id accordingly`() {
        // given
        ClientAndServer.startClientAndServer(configuration.port).use {
            it.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/$index/$doctype")
            ).respond(
                HttpResponse.response().withBody(saveResponseJson)
            )
            val entity = Entity("foo")

            // when
            val saveresponse = repository.save(entity, null)

            // then
            saveresponse shouldEqual expectedId
            it.verify(HttpRequest.request().withBody(mapper.writeValueAsString(entity)))
        }
    }

    @Test
    fun `should PUT against ES when saving with ID, simply returns the same id again`() {
        // given
        ClientAndServer.startClientAndServer(configuration.port).use {
            it.`when`(
                HttpRequest.request()
                    .withMethod("PUT")
                    .withPath("/$index/$doctype/$expectedId")
            ).respond(
                HttpResponse.response().withBody(saveResponseJson)
            )
            val entity = Entity("foo")

            // when
            val saveresponse = repository.save(
                entity,
                expectedId
            )

            // then
            saveresponse shouldEqual expectedId
            it.verify(HttpRequest.request().withBody(mapper.writeValueAsString(entity)))
        }
    }

    data class Entity(val value: String)
}