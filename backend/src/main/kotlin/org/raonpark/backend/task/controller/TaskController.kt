package org.raonpark.backend.task.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.raonpark.backend.common.page.PageResponse
import org.raonpark.backend.task.dto.ChangeStatusRequest
import org.raonpark.backend.task.dto.CreateTaskRequest
import org.raonpark.backend.task.dto.TaskDetail
import org.raonpark.backend.task.dto.TaskListItem
import org.raonpark.backend.task.dto.TaskListQuery
import org.raonpark.backend.task.dto.UpdateTaskRequest
import org.raonpark.backend.task.enums.TaskStatus
import org.raonpark.backend.task.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
@Validated
class TaskController(
    private val taskService: TaskService
) {
    @GetMapping
    fun getAllTasks(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(required = false) status: TaskStatus?,
        @RequestParam(required = false) assigneeId: UUID?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "createdAt,desc") sort: String,
    ): PageResponse<TaskListItem> {
        return taskService.getAllTasks(
            TaskListQuery(
                page = page,
                size = size,
                status = status,
                assigneeId = assigneeId,
                keyword = keyword,
                sort = sort
            )
        )
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: UUID): TaskDetail {
        return taskService.getTaskById(id)
    }

    @PostMapping
    fun saveTask(@RequestBody @Valid task: CreateTaskRequest): ResponseEntity<TaskDetail> {
        val taskDetail = taskService.saveTask(task)
        return ResponseEntity
            .created(URI.create("/api/tasks/${taskDetail.id}"))
            .body(taskDetail)
    }

    @PatchMapping("/{id}")
    fun updateTask(@PathVariable id: UUID, @RequestBody @Valid task: UpdateTaskRequest): TaskDetail {
        return taskService.updateTask(id, task)
    }

    @PatchMapping("/{id}/status")
    fun updateTaskStatus(@PathVariable id: UUID, @RequestBody @Valid status: ChangeStatusRequest): TaskDetail {
        return taskService.updateTaskStatus(id, status)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable id: UUID) {
        taskService.deleteTask(id)
    }
}
