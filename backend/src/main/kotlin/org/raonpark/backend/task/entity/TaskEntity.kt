package org.raonpark.backend.task.entity

import jakarta.persistence.*
import org.raonpark.backend.common.entity.BaseEntity
import org.raonpark.backend.task.enums.TaskPriority
import org.raonpark.backend.task.enums.TaskStatus
import org.springframework.data.annotation.PersistenceCreator
import java.util.*

@Entity
@Table(name = "task")
class TaskEntity @PersistenceCreator constructor (
    @Id
    private val id: UUID = UUID.randomUUID(),
    var title: String,
    @Column(name = "description", nullable = true)
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TaskStatus = TaskStatus.TODO,
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    var priority: TaskPriority = TaskPriority.MEDIUM,
    @Column(name = "assignee_id", nullable = true)
    var assigneeId: UUID? = null,
): BaseEntity<UUID>() {
    override fun getId(): UUID = id
}
