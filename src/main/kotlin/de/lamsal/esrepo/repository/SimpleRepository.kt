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
    override val configuration: ElasticSearchConfiguration,
    override val index: String,
    override val mapper: ObjectMapper = DefaultObjectMapper(),
    override val doctype: String = "_doc"
) : IRepository<T> {
    private val api: IHttpRequester = HttpRequester()

    private val hostUrl = configuration.run { "$protocol://$host:$port" }

    override fun save(obj: T, id: String?): String = mapper.readValue<SaveResponse>(
        if (id.isNullOrEmpty()) {
            api.post("$hostUrl/$index/$doctype", mapper.writeValueAsString(obj))
        } else {
            api.put("$hostUrl/$index/$doctype/$id", mapper.writeValueAsString(obj))
        })._id

    override fun getById(_id: String): EntityWrapper<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}