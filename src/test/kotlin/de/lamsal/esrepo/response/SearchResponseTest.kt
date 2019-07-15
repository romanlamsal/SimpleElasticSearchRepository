package de.lamsal.esrepo.response

import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.util.DefaultObjectMapper
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SearchResponseTest {
    private companion object {
        const val jsonWithoutScrollId = """{
            "took": 1,
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
                  "_id": "ort_Archshofen",
                  "_score": 2.3671236,
                  "_source": {
                    "type": "ort",
                    "value": "Archshofen"
                  }
                }
              ]
            }
          }"""
        const val jsonWithScrollId = """{
            "_scroll_id": "somescrollid",
            "took": 1,
            "timed_out": false,
            "_shards": {
              "total": 5,
              "successful": 5,
              "skipped": 0,
              "failed": 0
            },
            "hits": {
              "total": 3,
              "max_score": 2.730029,
              "hits": [
                {
                  "_index": "choices",
                  "_type": "choice",
                  "_id": "ort_Beuren",
                  "_score": 2.3671236,
                  "_source": {
                    "type": "ort",
                    "value": "Beuren"
                  }
                }
              ]
            }
          }"""
        val mapper = DefaultObjectMapper()
    }

    @Test
    fun `should be deserializable even without scroll_id`() {
        // given
        // jsonWithoutScrollId

        // when
        val deserialized = mapper.readValue<SearchResponse<Entity>>(jsonWithoutScrollId)

        // then
        assertNull(deserialized._scroll_id)
        deserialized.hits.apply {
            total shouldBe 5
            hits shouldContainAll listOf(GetResponse(Entity(type = "ort", value = "Archshofen"), "ort_Archshofen"))
        }
    }

    @Test
    fun `should be deserializable with scroll_id`() {
        // given
        // jsonWithScrollId

        // when
        val deserialized = mapper.readValue<SearchResponse<Entity>>(jsonWithScrollId)

        // then
        deserialized._scroll_id!! shouldBe "somescrollid"
        deserialized.hits.apply {
            total shouldBe 3
            hits shouldContainAll listOf(GetResponse(Entity(type = "ort", value = "Beuren"), "ort_Beuren"))
        }
    }

    data class Entity(
        val type: String = "", val value: String = ""
    )
}