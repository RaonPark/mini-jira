package org.raonpark.backend.users.repository

import org.raonpark.backend.users.entity.AppUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppUserRepository: JpaRepository<AppUserEntity, UUID> {
}
