package org.raonpark.backend.users.controller

import org.raonpark.backend.users.dto.UserSummary
import org.raonpark.backend.users.service.AppUserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class AppUserController(
    private val appUserService: AppUserService
) {

    @GetMapping
    fun getAllUsers(): List<UserSummary> {
        return appUserService.getUsers()
    }
}
