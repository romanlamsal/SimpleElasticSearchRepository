package de.lamsal.esrepo.response

data class SearchResponse<T>(
    val _scroll_id: String? = null,
    val hits: SearchResponseHits<T> = SearchResponseHits()
) {

    data class SearchResponseHits<S>(
        val total: Int = 0,
        val hits: List<GetResponse<S>> = emptyList()
    )
}