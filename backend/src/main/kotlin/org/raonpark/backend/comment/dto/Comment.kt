package org.raonpark.backend.comment.dto

import org.raonpark.backend.users.dto.UserSummary
import java.time.Instant
import java.util.UUID

data class Comment(
    val id: UUID,
    val taskId: UUID,
    val author: UserSummary,
    val content: String,
    val createdAt: Instant,
)
