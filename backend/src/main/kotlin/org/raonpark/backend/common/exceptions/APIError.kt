package org.raonpark.backend.common.exceptions

data class APIError(
    val code: String,
    val message: String,
    val fieldErrors: List<FieldError>? = null,
) {
    data class FieldError(
        val field: String, val message: String,
    )
}
