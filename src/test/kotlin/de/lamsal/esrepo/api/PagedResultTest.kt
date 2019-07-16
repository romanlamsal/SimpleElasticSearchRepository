package de.lamsal.esrepo.api

import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SearchResponse
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class PagedResultTest {
    private companion object {
        val entity = Entity("from hits")

        val searchResponseWithoutScroll = SearchResponse(
            null, SearchResponse.SearchResponseHits(
                10, listOf(
                    GetResponse(entity)
                )
            )
        )

        const val scrollId = "scrollId"
        val searchResponseWithScroll = SearchResponse(
            scrollId, SearchResponse.SearchResponseHits(
                10, listOf(
                    GetResponse(entity)
                )
            )
        )
    }

    @Test
    fun `should get hits from underlying SearchResponse with hits`() {
        // given
        val pagedResult = PagedResult(searchResponseWithoutScroll) { searchResponseWithoutScroll }

        // when
        val hits = pagedResult.hits

        // then
        hits shouldContainAll listOf(GetResponse(entity))
    }

    @Test
    fun `should get hits of all pages, when next page is requested, even when next page has empty list`() {
        // given
        val pagedResult = PagedResult(searchResponseWithScroll) {
            SearchResponse(null, SearchResponse.SearchResponseHits(10, emptyList()))
        }

        // when
        pagedResult.iterator().next()

        // then
        pagedResult.hits shouldBe searchResponseWithScroll.hits.hits
    }

    @Test
    fun `should iterate over all pages and stop when iterator_hasNext() = false`() {
        // given
        // total of 10 hits
        // first page: one entry, second page: 8 entries, third page: one entry.
        val pages = mutableListOf(
            SearchResponse(null, SearchResponse.SearchResponseHits(10, (0 until 10).map {
                GetResponse(entity)
            })),
            SearchResponse(null, SearchResponse.SearchResponseHits(10, (0 until 8).map {
                GetResponse(entity)
            })),
            SearchResponse(null, SearchResponse.SearchResponseHits(10, emptyList()))
        )

        val pagedResult = PagedResult(searchResponseWithScroll) {
            pages.removeAt(0)
        }

        // when
        val fetchedHits = pagedResult.hits

        // then
        fetchedHits shouldContainAll listOf(pages[0].hits.hits, pages[1].hits.hits, pages[2].hits.hits).flatten()
    }

    data class Entity(val value: String)
}