package org.raonpark.backend.comment.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.raonpark.backend.common.entity.BaseEntity
import java.util.UUID

@Entity
@Table(name = "task_comment")
class TaskCommentEntity(
    @Id
    private val id: UUID = UUID.randomUUID(),
    @Column(name = "task_id", nullable = false)
    var taskId: UUID,
    @Column(name = "author_id", nullable = false)
    var authorId: UUID,
    @Column(name = "content", nullable = false)
    var content: String,
): BaseEntity<UUID>() {
    override fun getId(): UUID = id
}
