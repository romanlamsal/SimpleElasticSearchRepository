package de.lamsal.esrepo.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.api.IHttpRequester
import de.lamsal.esrepo.util.DefaultObjectMapper
import de.lamsal.esrepo.ElasticSearchConfiguration
import de.lamsal.esrepo.api.QueryParams
import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SaveResponse
import de.lamsal.esrepo.response.SearchResponse

class SimpleRepository<T>(
    clazz: Class<T>,
    configuration: ElasticSearchConfiguration,
    private val index: String,
    private val mapper: ObjectMapper = DefaultObjectMapper(),
    private val doctype: String = "_doc"
) {
    private val hostUrl = configuration.run { "$protocol://$host:$port" }

    private val api: IHttpRequester = HttpRequester()

    private val classToGetResponse = mapper.typeFactory.constructParametricType(GetResponse::class.java, clazz)

    fun save(obj: T, id: String = ""): String = mapper.readValue<SaveResponse>(
        if (id.isEmpty()) {
            api.post("$hostUrl/$index/$doctype", mapper.writeValueAsString(obj))
        } else {
            api.put("$hostUrl/$index/$doctype/$id", mapper.writeValueAsString(obj))
        }
    )._id

    fun refresh() {
        api.post("$hostUrl/$index/_refresh")
    }

    fun getById(id: String): T? = try {
        api.get("$hostUrl/$index/$doctype/$id").let {
            mapper.readValue<GetResponse<T>>(it, classToGetResponse)._source
        }
    } catch (error: Exception) {
        null
    }

    private val classToSearchResponse = mapper.typeFactory.constructParametricType(SearchResponse::class.java, clazz)
    fun executeQuery(query: String): SearchResponse<T> {
        val size: Int = mapper.readValue<Map<String, Any>>(api.get("$hostUrl/$index/$doctype/_count"))["count"] as Int
        return mapper.readValue<SearchResponse<T>>(
            api.post("$hostUrl/$index/$doctype/_search${QueryParams(size = size)}", query), classToSearchResponse
        )
    }
}