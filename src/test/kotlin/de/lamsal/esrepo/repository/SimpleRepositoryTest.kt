package de.lamsal.esrepo.repository

import de.lamsal.esrepo.ElasticSearchConfiguration
import de.lamsal.esrepo.api.IHttpRequester
import de.lamsal.esrepo.api.QueryParams
import de.lamsal.esrepo.dsl.query
import de.lamsal.esrepo.dsl.terms
import de.lamsal.esrepo.exception.HttpError
import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SearchResponse
import de.lamsal.esrepo.util.DefaultObjectMapper
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.OverrideMockKs
import io.mockk.mockk
import io.mockk.verify
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
            "_id" : "0",
            "_version" : 1,
            "_seq_no" : 10,
            "_primary_term" : 1,
            "found": true,
            "_source" : ${mapper.writeValueAsString(entity)}
        }"""

        const val scrollId = """superlongscrollid"""
        const val searchResponseJson = """{
          "_scroll_id": "$scrollId",
          "took": 6,
          "timed_out": false,
          "_shards": {
            "total": 5,
            "successful": 5,
            "skipped": 0,
            "failed": 0
          },
          "hits": {
            "total": 5,
            "max_score": 2.730029,
            "hits": [
              {
                "_index": "choices",
                "_type": "choice",
                "_id": "some_id",
                "_score": 2.730029,
                "_source": {
                  "value": "Foo"
                }
              },
              {
                "_index": "choices",
                "_type": "choice",
                "_id": "other_id",
                "_score": 2.4849067,
                "_source": {
                  "value": "Bar"
                }
              }
            ]
          }
        }"""
        const val searchResponseJsonPageTwo = """{
          "took": 6,
          "timed_out": false,
          "_shards": {
            "total": 5,
            "successful": 5,
            "skipped": 0,
            "failed": 0
          },
          "hits": {
            "total": 5,
            "max_score": 2.730029,
            "hits": []
          }
        }"""
    }

    @OverrideMockKs
    private lateinit var repository: SimpleRepository<Entity>

    private lateinit var apiMock: IHttpRequester

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
        val saveresponse = repository.save(entity)

        // then
        saveresponse shouldBe expectedId
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
        saveresponse shouldBe expectedId
    }

    @Test
    fun `should return null, when getById can not find anything`() {
        // given
        every { apiMock.get(any()) } throws HttpError(Exception("Some HttpError."))

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

    @Test
    fun `should be able to execute query and fetch all results at once`() {
        // given
        val query = query { terms { "value" to listOf("Foo", "Bar") } }.toString()
        val size = 2
        every { apiMock.get("$hostUrl/$index/$doctype/_count") } returns """{"count": $size,"_shards": {"total": 5,"successful": 5,"skipped": 0,"failed": 0}}"""
        every { apiMock.post("$hostUrl/$index/$doctype/_search?size=$size", query) } returns searchResponseJson

        // when
        val queryResult: SearchResponse<Entity> = repository.executeQuery(query)

        // then
        val expectedHits = listOf(GetResponse(Entity("Foo"), "some_id"), GetResponse(Entity("Bar"), "other_id"))
        queryResult.hits.hits shouldContainAll expectedHits
    }

    @Test
    fun `force refresh`() {
        // given
        every { apiMock.post("$hostUrl/$index/_refresh") } returns "OK"

        // when
        repository.refresh()

        // then
        verify { apiMock.post("$hostUrl/$index/_refresh", null) }
    }

    data class Entity(val value: String)
}