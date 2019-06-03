package de.lamsal.esrepo.api

import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SearchResponse

class PagedResult<T>(
    searchResponse: SearchResponse<T>,
    private val onNextPage: (scrollId: String) -> SearchResponse<T>
) : Iterable<List<GetResponse<T>>> {
    val hits: MutableList<GetResponse<T>> = searchResponse.hits.hits.toMutableList()

    private val scrollId = searchResponse._scroll_id
    private val pages: MutableList<SearchResponse<T>> = mutableListOf(searchResponse)

    override fun iterator(): Iterator<List<GetResponse<T>>> = PageIterator(0)

    private inner class PageIterator(private var nextPage: Int) : Iterator<List<GetResponse<T>>> {
        override fun hasNext(): Boolean = pages[nextPage].hits.hits.isNotEmpty()

        override fun next(): List<GetResponse<T>> {
            return pages[nextPage++].hits.hits.also {
                if (scrollId != null && nextPage == pages.size) {
                    onNextPage(scrollId).apply {
                        pages.add(this)
                        this@PagedResult.hits.addAll(hits.hits)
                    }
                }
            }
        }
    }
}