package org.raonpark.backend.comment.controller

import jakarta.validation.Valid
import org.raonpark.backend.comment.dto.Comment
import org.raonpark.backend.comment.dto.CreateCommentRequest
import org.raonpark.backend.comment.service.TaskCommentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TaskCommentController(
    private val taskCommentService: TaskCommentService
) {
    @GetMapping("/api/tasks/{taskId}/comments")
    fun getComments(@PathVariable taskId: UUID) = taskCommentService.getAllCommentsByTaskId(taskId)

    @PostMapping("/api/tasks/{taskId}/comments")
    fun saveComment(@PathVariable taskId: UUID, @RequestBody @Valid comment: CreateCommentRequest): ResponseEntity<Comment> {
        val comment = taskCommentService.saveComment(taskId, comment)

        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    @DeleteMapping("/api/tasks/{taskId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(@PathVariable taskId: UUID, @PathVariable id: UUID) {
        taskCommentService.deleteComment(taskId, id)
    }
}
