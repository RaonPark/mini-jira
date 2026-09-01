package org.raonpark.backend.task.service

import org.raonpark.backend.common.page.PageResponse
import org.raonpark.backend.task.dto.TaskDetail
import org.raonpark.backend.task.dto.TaskListItem
import org.raonpark.backend.task.dto.TaskListQuery
import org.raonpark.backend.task.repository.TaskRepository
import org.raonpark.backend.task.repository.jooq.TaskQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskQueryRepository: TaskQueryRepository,
) {
    @Transactional(readOnly = true)
    fun getAllTasks(query: TaskListQuery): PageResponse<TaskListItem> {
        return taskQueryRepository.findPage(query)
    }

    @Transactional(readOnly = true)
    fun getTaskById(id: UUID): TaskDetail {
        return taskQueryRepository.findTaskById(id)
    }
}
