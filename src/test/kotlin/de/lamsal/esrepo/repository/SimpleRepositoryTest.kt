package de.lamsal.esrepo.repository

import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.ElasticSearchConfiguration
import de.lamsal.esrepo.api.PagedResult
import de.lamsal.esrepo.api.QueryParams
import de.lamsal.esrepo.dsl.query
import de.lamsal.esrepo.dsl.terms
import de.lamsal.esrepo.exception.HttpError
import de.lamsal.esrepo.response.GetResponse
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
                "_id": "kjasld",
                "_score": 2.730029,
                "_source": {
                  "value": "Foo"
                }
              },
              {
                "_index": "choices",
                "_type": "choice",
                "_id": "24n11l",
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
    fun `should be able to execute query with params`() {
        // given
        val query = query { terms { "value" to listOf("Foo", "Bar") } }.toString()
        every { apiMock.post("$hostUrl/$index/$doctype/_search?scroll=5m&size=2", query) } returns searchResponseJson
        every { apiMock.get("$hostUrl/_search/scroll?scroll_id=$scrollId") } returns searchResponseJsonPageTwo

        // when
        val pagedResult: PagedResult<Entity> = repository.executeQuery(query, QueryParams(size = 2, scroll = "5m"))

        // then
        val expectedHits = listOf(GetResponse(Entity("Foo")), GetResponse(Entity("Bar")))
        assertEquals(expectedHits, pagedResult.flatten())
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