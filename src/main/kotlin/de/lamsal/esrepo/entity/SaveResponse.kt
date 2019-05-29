package de.lamsal.esrepo.entity

enum class SAVERESULT {
    CREATED
}

data class SaveResponse(
    val _id: String,
    val result: SAVERESULT
)