package org.raonpark.backend.comment.service

import org.raonpark.backend.comment.dto.Comment
import org.raonpark.backend.comment.dto.CreateCommentRequest
import org.raonpark.backend.comment.repository.TaskCommentRepository
import org.raonpark.backend.comment.repository.jooq.TaskCommentQueryRepository
import org.raonpark.backend.common.exceptions.TaskCommentNotFoundException
import org.raonpark.backend.common.exceptions.TaskNotFoundException
import org.raonpark.backend.task.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

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

    @Transactional
    fun saveComment(taskId: UUID, comment: CreateCommentRequest): Comment {
        if(!taskRepository.existsById(taskId)) throw TaskNotFoundException(taskId)

        val id = taskCommentRepository.saveAndFlush(comment.toEntity(taskId)).id

        return taskCommentQueryRepository.findCommentById(id)
    }

    @Transactional
    fun deleteComment(taskId: UUID, id: UUID) {
        if(!taskRepository.existsById(taskId))
            throw TaskNotFoundException(taskId)

        if(taskCommentRepository.deleteByIdAndTaskId(id, taskId) == 0L)
            throw TaskCommentNotFoundException(id)
    }
}
