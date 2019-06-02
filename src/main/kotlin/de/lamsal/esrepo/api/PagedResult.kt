package de.lamsal.esrepo.api

import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SearchResponse

class PagedResult<T>(
    private var searchResponse: SearchResponse<T>,
    private val onNextPage: (scrollId: String) -> SearchResponse<T>
) : Iterable<List<GetResponse<T>>> {
    private val scrollId = searchResponse._scroll_id

    private var numMissingHits = searchResponse.hits.total

    private val pages: MutableList<SearchResponse<T>> = mutableListOf(searchResponse)

    var hits: List<GetResponse<T>> = searchResponse.hits.hits
        private set

    override fun iterator(): Iterator<List<GetResponse<T>>> = PageIterator()

    private inner class PageIterator : Iterator<List<GetResponse<T>>> {
        private var nextPage = 0
        override fun hasNext(): Boolean = numMissingHits > 0

        override fun next(): List<GetResponse<T>> {
            hits = searchResponse.hits.hits
            return hits.also {
                numMissingHits -= searchResponse.hits.hits.size
                if (numMissingHits > 0) {
                    searchResponse = onNextPage(scrollId!!)
                }
            }
        }
    }
}