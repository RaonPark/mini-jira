package org.raonpark.backend.common.page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
) {
    val totalPages: Int = if (size <= 0) 0 else ((totalElements + size - 1) / size)
}
