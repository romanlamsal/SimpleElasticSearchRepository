package de.lamsal.esrepo.repository

import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.ElasticSearchConfiguration
import de.lamsal.esrepo.util.DefaultObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.OverrideMockKs
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SimpleRepositoryTest {
    private companion object {
        val configuration = ElasticSearchConfiguration(host = "localhost", port = 9200)
        val hostUrl = configuration.run { "$protocol://$host:$port" }

        val mapper = DefaultObjectMapper() // default value in SimpleRepository

        val entity = Entity("foo")

        const val index = "testindex"
        const val doctype = "_doc" // default value in SimpleRepository
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

        val getResponseJson = """{
    "_index" : "twitter",
    "_type" : "_doc",
    "_id" : "$expectedId",
    "_version" : 1,
    "found": true,
    "_source" : ${mapper.writeValueAsString(entity)}
}"""
    }

    @OverrideMockKs
    private lateinit var repository: SimpleRepository<Entity>

    private lateinit var apiMock: HttpRequester

    @BeforeEach
    fun beforeEach() {
        apiMock = mockk()
        repository = SimpleRepository(
            clazz = Entity::class.java,
            configuration = configuration,
            index = index
        )

        MockKAnnotations.init(this)
    }

    @Test
    fun `should POST against ES when saving without ID, sets responss's id accordingly`() {
        // given
        every {
            apiMock.post(
                "$hostUrl/$index/$doctype", any()
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
                "$hostUrl/$index/$doctype/$expectedId", any()
            )
        } returns saveResponseJson

        // when
        val saveresponse = repository.save(entity, expectedId)

        // then
        verify { apiMock.put(any(), mapper.writeValueAsString(entity)) }
        saveresponse shouldEqual expectedId
    }

    @Test
    fun `should return null, when getById can not find anything`() {
        // given
        every { apiMock.get(any()) } returns null

        // when
        val response = repository.getById("foo")

        // then
        assertNull(response)
    }

    @Test
    fun `should return entity, when getById is called successfully`() {
        // given
        every { apiMock.get("$hostUrl/$index/$doctype/$expectedId") } returns getResponseJson

        // when
        val response = repository.getById(expectedId)

        // then
        assertEquals(entity, response)
    }

    data class Entity(val value: String)
}