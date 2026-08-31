package org.raonpark.backend.users.repository.jooq

import org.jooq.DSLContext
import org.jooq.Records
import org.raonpark.backend.jooq.tables.AppUser.APP_USER
import org.raonpark.backend.users.dto.UserSummary
import org.springframework.stereotype.Repository

@Repository
class AppUserQueryRepository(
    private val dslContext: DSLContext
) {
    fun findAll(): List<UserSummary> {
        return dslContext
            .select(
                APP_USER.ID,
                APP_USER.NAME,
                APP_USER.AVATAR_COLOR,
            )
            .from(APP_USER)
            .orderBy(APP_USER.NAME)
            .fetch(Records.mapping(::UserSummary))
    }
}
