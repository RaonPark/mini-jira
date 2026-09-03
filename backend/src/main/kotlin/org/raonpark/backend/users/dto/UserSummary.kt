package org.raonpark.backend.users.dto

import java.util.UUID

data class UserSummary(
    val id: UUID,
    val name: String,
    val avatarColor: String,
)
