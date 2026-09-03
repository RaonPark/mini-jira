package org.raonpark.backend.task.repository.jooq

import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.SortField
import org.jooq.impl.DSL
import org.raonpark.backend.common.exceptions.TaskNotFoundException
import org.raonpark.backend.common.page.PageResponse
import org.raonpark.backend.jooq.tables.AppUser.APP_USER
import org.raonpark.backend.jooq.tables.Task.TASK
import org.raonpark.backend.jooq.tables.TaskComment.TASK_COMMENT
import org.raonpark.backend.task.dto.TaskDetail
import org.raonpark.backend.task.dto.TaskListItem
import org.raonpark.backend.task.dto.TaskListQuery
import org.raonpark.backend.users.dto.UserSummary
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TaskQueryRepository(
    private val dslContext: DSLContext
) {
    fun findPage(
        query: TaskListQuery
    ): PageResponse<TaskListItem> {
        val condition = buildCondition(query)

        val content = dslContext
            .select(
                TASK.ID,
                TASK.TITLE,
                TASK.STATUS,
                TASK.PRIORITY,
                DSL.row(APP_USER.ID, APP_USER.NAME, APP_USER.AVATAR_COLOR)
                    .mapping { id, name, color -> if (id == null) null else UserSummary(id, name, color) },
                DSL.selectCount().from(TASK_COMMENT)
                    .where(TASK_COMMENT.TASK_ID.eq(TASK.ID))
                    .asField<Int>("comment_count"),
                TASK.CREATED_AT.convertFrom { it.toInstant() },
                TASK.UPDATED_AT.convertFrom { it.toInstant() },
            )
            .from(TASK)
            .leftJoin(APP_USER).on(TASK.ASSIGNEE_ID.eq(APP_USER.ID))
            .where(condition)
            .orderBy(query.toSortFields())
            .limit(query.size)
            .offset(query.page * query.size)
            .fetch(Records.mapping(::TaskListItem))

        val totalElements = dslContext.fetchCount(TASK, condition)

        return PageResponse(content, query.page, query.size, totalElements)
    }

    private fun buildCondition(query: TaskListQuery): Condition = DSL.and(
        listOfNotNull(
            query.status?.let { TASK.STATUS.eq(it) },
            query.assigneeId?.let { TASK.ASSIGNEE_ID.eq(it) },
            query.keyword?.takeIf { it.isNotBlank() }?.let { TASK.TITLE.containsIgnoreCase(it) }
        )
    )

    private fun TaskListQuery.toSortFields(): List<SortField<*>> = when(sort) {
        "createdAt,asc" -> listOf(TASK.CREATED_AT.asc())
        "updatedAt,desc" -> listOf(TASK.UPDATED_AT.desc())
        "priority,desc" -> listOf(TASK.PRIORITY.desc())
        else -> listOf(TASK.CREATED_AT.desc())
    } + TASK.ID.desc()

    fun findTaskById(id: UUID): TaskDetail {
        return dslContext.select(
            TASK.ID,
            TASK.TITLE,
            TASK.DESCRIPTION,
            TASK.STATUS,
            TASK.PRIORITY,
            DSL.row(APP_USER.ID, APP_USER.NAME, APP_USER.AVATAR_COLOR)
                .mapping { id, name, color -> if (id == null) null else UserSummary(id, name, color) },
            TASK.CREATED_AT.convertFrom { it.toInstant() },
            TASK.UPDATED_AT.convertFrom { it.toInstant() },)
            .from(TASK)
            .leftJoin(APP_USER).on(TASK.ASSIGNEE_ID.eq(APP_USER.ID))
            .where(TASK.ID.eq(id))
            .fetchOne(Records.mapping(::TaskDetail)) ?: throw TaskNotFoundException(id)
    }
}
