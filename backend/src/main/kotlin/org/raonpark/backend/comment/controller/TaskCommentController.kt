package org.raonpark.backend.comment.controller

import org.raonpark.backend.comment.service.TaskCommentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TaskCommentController(
    private val taskCommentService: TaskCommentService
) {
    @GetMapping("/api/tasks/{id}/comments")
    fun getComments(@PathVariable("id") taskId: UUID) = taskCommentService.getAllCommentsByTaskId(taskId)
}
