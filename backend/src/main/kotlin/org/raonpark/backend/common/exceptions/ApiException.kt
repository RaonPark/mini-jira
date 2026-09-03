package org.raonpark.backend.common.exceptions

import org.springframework.http.HttpStatus
import java.util.UUID

abstract class ApiException(
    val status: HttpStatus,
    val code: String,
    override val message: String,
): RuntimeException(message)

class TaskNotFoundException(val taskId: UUID):
    ApiException(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "해당 작업을 찾을 수 없습니다.")

class TaskCommentNotFoundException(val commentId: UUID):
    ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "해당 댓글을 찾을 수 없습니다.")

class UserNotFoundException(val userId: UUID):
    ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다.")
