package de.lamsal.esrepo.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.api.IHttpRequester
import de.lamsal.esrepo.util.DefaultObjectMapper
import de.lamsal.esrepo.ElasticSearchConfiguration
import de.lamsal.esrepo.response.GetResponse
import de.lamsal.esrepo.response.SaveResponse

class SimpleRepository<T> (
    clazz: Class<T>,
    configuration: ElasticSearchConfiguration,
    private val index: String,
    private val mapper: ObjectMapper = DefaultObjectMapper(),
    private val doctype: String = "_doc"
) {
    private val hostUrl = configuration.run { "$protocol://$host:$port" }

    private val api: IHttpRequester = HttpRequester()

    private val classToMap = mapper.typeFactory.constructParametricType(GetResponse::class.java, clazz)

    fun save(obj: T, id: String?): String = mapper.readValue<SaveResponse>(
        if (id.isNullOrEmpty()) {
            api.post("$hostUrl/$index/$doctype", mapper.writeValueAsString(obj))!!
        } else {
            api.put("$hostUrl/$index/$doctype/$id", mapper.writeValueAsString(obj))!!
        })._id

    fun getById(id: String): T? = api.get("$hostUrl/$index/$doctype/$id")?.let {
        mapper.readValue<GetResponse<T>>(it, classToMap)._source
    }
}