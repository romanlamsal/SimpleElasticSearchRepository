package de.lamsal.esrepo.response

import com.fasterxml.jackson.module.kotlin.readValue
import de.lamsal.esrepo.util.DefaultObjectMapper
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

internal class SaveResponseTest {

    private companion object {
        const val jsonString = """{"_id":"some_id","result":"created"}"""
        val saveResponse = SaveResponse("some_id")
    }

    private val mapper = DefaultObjectMapper()

    @Test
    fun `should deserialize SaveResponse from JSON`() {
        mapper.readValue<SaveResponse>(jsonString) shouldEqual saveResponse
    }


}