package de.lamsal.esrepo.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.api.HttpRequester
import de.lamsal.esrepo.api.IHttpRequester
import de.lamsal.esrepo.util.DefaultObjectMapper
import de.lamsal.esrepo.configuration.ElasticSearchConfiguration
import de.lamsal.esrepo.entity.EntityWrapper
import de.lamsal.esrepo.entity.SaveResponse

class SimpleRepository<T> (
    clazz: Class<T>,
    configuration: ElasticSearchConfiguration,
    index: String,
    mapper: ObjectMapper = DefaultObjectMapper(),
    doctype: String = "_doc"
) : IRepository<T>(configuration, index, doctype, mapper) {
    private val api: IHttpRequester = HttpRequester()

    private val classToMap = mapper.typeFactory.constructParametricType(EntityWrapper::class.java, clazz)

    override fun save(obj: T, id: String?): String = mapper.readValue<SaveResponse>(
        if (id.isNullOrEmpty()) {
            api.post("$hostUrl/$index/$doctype", mapper.writeValueAsString(obj))!!
        } else {
            api.put("$hostUrl/$index/$doctype/$id", mapper.writeValueAsString(obj))!!
        })._id

    override fun getById(id: String): T? = api.get("$hostUrl/$index/$doctype/$id")?.let {
        mapper.readValue<EntityWrapper<T>>(it, classToMap)._source
    }
}