package org.raonpark.backend.users.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.raonpark.backend.common.entity.BaseEntity
import java.util.UUID

@Entity
@Table(name = "app_user")
class AppUserEntity(
    @Id
    private val id: UUID = UUID.randomUUID(),
    @Column(name = "name", nullable = false, length = 50)
    var name: String,
    @Column(name = "email", nullable = false, length = 255, unique = true)
    var email: String,
    @Column(name = "avatar_color", nullable = false, length = 7)
    var avatarColor: String,
): BaseEntity<UUID>() {
    override fun getId(): UUID = id
}
