package org.raonpark.backend.users.service

import org.raonpark.backend.users.dto.UserSummary
import org.raonpark.backend.users.repository.AppUserRepository
import org.raonpark.backend.users.repository.jooq.AppUserQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val appUserQueryRepository: AppUserQueryRepository
) {
    @Transactional(readOnly = true)
    fun getUsers(): List<UserSummary> {
        return appUserQueryRepository.findAll()
    }
}
