package org.raonpark.backend.comment.dto

import jakarta.validation.constraints.NotBlank
import org.raonpark.backend.comment.entity.TaskCommentEntity
import java.util.UUID

data class CreateCommentRequest(
    val authorId: UUID,
    @field:NotBlank
    val content: String,
) {
    fun toEntity(taskId: UUID): TaskCommentEntity {
        return TaskCommentEntity(
            authorId = authorId,
            content = content,
            taskId = taskId
        )
    }
}
