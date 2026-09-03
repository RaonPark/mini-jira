package org.raonpark.backend.task.service

import org.raonpark.backend.common.exceptions.TaskNotFoundException
import org.raonpark.backend.common.exceptions.UserNotFoundException
import org.raonpark.backend.common.page.PageResponse
import org.raonpark.backend.task.dto.*
import org.raonpark.backend.task.repository.TaskRepository
import org.raonpark.backend.task.repository.jooq.TaskQueryRepository
import org.raonpark.backend.users.repository.AppUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskQueryRepository: TaskQueryRepository,
    private val appUserRepository: AppUserRepository,
) {
    @Transactional(readOnly = true)
    fun getAllTasks(query: TaskListQuery): PageResponse<TaskListItem> {
        return taskQueryRepository.findPage(query)
    }

    @Transactional(readOnly = true)
    fun getTaskById(id: UUID): TaskDetail {
        return taskQueryRepository.findTaskById(id)
    }

    @Transactional
    fun saveTask(task: CreateTaskRequest): TaskDetail {
        task.assigneeId?.let {
            if (!appUserRepository.existsById(it)) {
                throw UserNotFoundException(it)
            }
        }
        val id = taskRepository.saveAndFlush(task.toEntity()).id

        return taskQueryRepository.findTaskById(id)
    }

    @Transactional
    fun updateTask(id: UUID, task: UpdateTaskRequest): TaskDetail {
        val taskEntity = taskRepository.findById(id).orElseThrow { TaskNotFoundException(id) }
        task.assigneeId?.ifPresent { assigneeId ->
            if (!appUserRepository.existsById(assigneeId)) {
                throw UserNotFoundException(assigneeId)
            }
        }

        task.title?.let { taskEntity.title = it }
        task.description?.let { taskEntity.description = it.orElse(null) }
        task.priority?.let { taskEntity.priority = it }
        task.assigneeId?.let { taskEntity.assigneeId = it.orElse(null) }

        taskRepository.flush()
        return taskQueryRepository.findTaskById(id)
    }

    @Transactional
    fun updateTaskStatus(id: UUID, status: ChangeStatusRequest): TaskDetail {
        val taskEntity = taskRepository.findById(id).orElseThrow { TaskNotFoundException(id) }

        taskEntity.status = status.status

        taskRepository.flush()
        return taskQueryRepository.findTaskById(id)
    }

    @Transactional
    fun deleteTask(id: UUID) {
        if(!taskRepository.existsById(id)) throw TaskNotFoundException(id)
        taskRepository.deleteById(id)
    }
}
