package de.lamsal.esrepo.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.configuration.ElasticSearchConfiguration
import de.lamsal.esrepo.entity.EntityWrapper

abstract class IRepository<T> (
    configuration: ElasticSearchConfiguration,
    val index: String,
    val doctype: String,
    val mapper: ObjectMapper
) {
    protected val hostUrl = configuration.run { "$protocol://$host:$port" }

    abstract fun save(obj: T, id: String?): String

    abstract fun getById(id: String): T?

}