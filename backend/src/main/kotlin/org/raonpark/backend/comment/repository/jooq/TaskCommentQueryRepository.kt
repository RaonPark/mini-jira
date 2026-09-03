package org.raonpark.backend.comment.repository.jooq

import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.impl.DSL
import org.raonpark.backend.comment.dto.Comment
import org.raonpark.backend.common.exceptions.TaskCommentNotFoundException
import org.raonpark.backend.jooq.tables.AppUser.APP_USER
import org.raonpark.backend.jooq.tables.TaskComment.TASK_COMMENT
import org.raonpark.backend.users.dto.UserSummary
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TaskCommentQueryRepository(
    private val dslContext: DSLContext
) {
    fun findAll(taskId: UUID): List<Comment>{
        return dslContext
            .select(
                TASK_COMMENT.ID,
                TASK_COMMENT.TASK_ID,
                DSL.row(APP_USER.ID, APP_USER.NAME, APP_USER.AVATAR_COLOR)
                    .mapping(::UserSummary),
                TASK_COMMENT.CONTENT,
                TASK_COMMENT.CREATED_AT.convertFrom { it.toInstant() },
            )
            .from(TASK_COMMENT)
            .join(APP_USER).on(TASK_COMMENT.AUTHOR_ID.eq(APP_USER.ID))
            .where(TASK_COMMENT.TASK_ID.eq(taskId))
            .orderBy(TASK_COMMENT.CREATED_AT.asc(), TASK_COMMENT.ID.asc())
            .fetch(Records.mapping(::Comment))
    }

    fun findCommentById(id: UUID): Comment {
        return dslContext
            .select(
                TASK_COMMENT.ID,
                TASK_COMMENT.TASK_ID,
                DSL.row(APP_USER.ID, APP_USER.NAME, APP_USER.AVATAR_COLOR)
                    .mapping(::UserSummary),
                TASK_COMMENT.CONTENT,
                TASK_COMMENT.CREATED_AT.convertFrom { it.toInstant() },
            )
            .from(TASK_COMMENT)
            .join(APP_USER).on(TASK_COMMENT.AUTHOR_ID.eq(APP_USER.ID))
            .where(TASK_COMMENT.ID.eq(id))
            .orderBy(TASK_COMMENT.CREATED_AT.asc(), TASK_COMMENT.ID.asc())
            .fetchOne(Records.mapping(::Comment)) ?: throw TaskCommentNotFoundException(id)
    }
}
