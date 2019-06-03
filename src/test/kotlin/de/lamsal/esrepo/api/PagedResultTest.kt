package de.lamsal.esrepo.api

import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SearchResponse
import org.amshove.kluent.shouldEqual
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
        hits shouldEqual listOf(GetResponse(entity))
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
        pagedResult.hits shouldEqual searchResponseWithScroll.hits.hits
    }

    @Test
    fun `should iterate over all pages and stop when iterator_hasNext() = false`() {
        // given
        // total of 10 hits
        // first page: one entry, second page: 8 entries, third page: one entry.
        val pages = listOf(
            searchResponseWithScroll,
            SearchResponse(null, SearchResponse.SearchResponseHits(10, (0 until 8).map {
                GetResponse(entity)
            })),
            searchResponseWithoutScroll,
            SearchResponse(null, SearchResponse.SearchResponseHits(10, emptyList()))
        )
        var currentPage = 0

        val pagedResult = PagedResult(searchResponseWithScroll) {
            pages[++currentPage]
        }

        // when
        val mappedPages = pagedResult.map { it }

        // then
        mappedPages shouldEqual listOf(pages[0].hits.hits, pages[1].hits.hits, pages[2].hits.hits)
    }

    @Test
    fun `should iterate TWICE over all pages, will both times return the same result`() {
        // given
        // total of 10 hits
        // first page: one entry, second page: 8 entries, third page: one entry.
        val pages = listOf(
            searchResponseWithScroll,
            SearchResponse(null, SearchResponse.SearchResponseHits(10, (1..8).map {
                GetResponse(Entity("$it"))
            })),
            searchResponseWithoutScroll,
            SearchResponse(null, SearchResponse.SearchResponseHits(10, emptyList()))
        )
        var currentPage = 0

        val pagedResult = PagedResult(searchResponseWithScroll) {
            pages[++currentPage]
        }

        // when
        val mappedPages = pagedResult.map { it }
        currentPage = 0

        // then
        mappedPages shouldEqual pagedResult.map { it }
        mappedPages.flatten() shouldEqual pagedResult.hits
        mappedPages.flatten().size shouldEqual 10
    }

    data class Entity(val value: String)
}