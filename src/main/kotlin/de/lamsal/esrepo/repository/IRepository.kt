package de.lamsal.esrepo.repository

import com.fasterxml.jackson.databind.ObjectMapper
import de.lamsal.esrepo.configuration.ElasticSearchConfiguration
import de.lamsal.esrepo.entity.EntityWrapper

interface IRepository<T> {
    val configuration: ElasticSearchConfiguration

    val index: String

    val doctype: String

    val mapper: ObjectMapper

    fun save(obj: T, id: String?): String

    fun getById(_id: String): EntityWrapper<T>
}