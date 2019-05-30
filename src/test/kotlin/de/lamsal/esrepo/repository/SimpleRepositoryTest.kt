package de.lamsal.esrepo.repository

import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.configuration.ElasticSearchConfiguration
import de.lamsal.esrepo.util.DefaultObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.OverrideMockKs
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

        val entity = Entity("foo")
    }

    @OverrideMockKs
    private lateinit var repository: SimpleRepository<Entity>

    private lateinit var apiMock: HttpRequester

    @BeforeEach
    fun beforeEach() {
        apiMock = mockk()
        repository = SimpleRepository(
            configuration = configuration,
            mapper = mapper,
            index = index,
            doctype = doctype
        )

        MockKAnnotations.init(this)
    }

    @Test
    fun `should POST against ES when saving without ID, sets responss's id accordingly`() {
        // given
        every {
            apiMock.post(
                configuration.run { "$protocol://$host:$port" } + "/$index/$doctype", any()
            )
        } returns saveResponseJson

        // when
        val saveresponse = repository.save(entity, null)

        // then
        saveresponse shouldEqual expectedId

        verify { apiMock.post(any(), mapper.writeValueAsString(entity)) }
    }

    @Test
    fun `should PUT against ES when saving with ID, simply returns the same id again`() {
        // given
        every {
            apiMock.put(
                configuration.run { "$protocol://$host:$port" } + "/$index/$doctype/$expectedId", any()
                )
        } returns saveResponseJson

        // when
        val saveresponse = repository.save(entity, expectedId)

        // then
        verify { apiMock.put(any(), mapper.writeValueAsString(entity)) }

        saveresponse shouldEqual expectedId
    }

    data class Entity(val value: String)
}