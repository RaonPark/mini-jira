package org.raonpark.backend.comment.service

import org.raonpark.backend.comment.repository.TaskCommentRepository
import org.raonpark.backend.comment.repository.jooq.TaskCommentQueryRepository
import org.raonpark.backend.task.exceptions.TaskNotFoundException
import org.raonpark.backend.task.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TaskCommentService(
    private val taskCommentQueryRepository: TaskCommentQueryRepository,
    private val taskCommentRepository: TaskCommentRepository,
    private val taskRepository: TaskRepository,
) {
    @Transactional(readOnly = true)
    fun getAllCommentsByTaskId(taskId: UUID) =
        if(taskRepository.existsById(taskId))
            taskCommentQueryRepository.findAll(taskId)
        else throw TaskNotFoundException(taskId)
}
