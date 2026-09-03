package org.raonpark.backend.comment.repository

import org.raonpark.backend.comment.entity.TaskCommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskCommentRepository: JpaRepository<TaskCommentEntity, UUID> {
    fun deleteByIdAndTaskId(id: UUID, taskId: UUID): Long
}
