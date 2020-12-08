package com.epam.brn.integration

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.UserAccountRepository
import com.google.gson.Gson
import org.amshove.kluent.internal.assertNotSame
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserDetailsControllerIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    internal val email: String = "test@test.test"
    internal val password: String = "test"
    private val baseUrl = "/users"
    private val currentUserBaseUrl = "$baseUrl/current"

    @Test
    fun `update avatar for current user`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val resultAction = mockMvc.perform(
            put("$currentUserBaseUrl/avatar")
                .queryParam("avatar", "/pictures/testAvatar")
        )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val resultUser: UserAccountResponse =
            objectMapper.readValue(Gson().toJson(baseResponseDto.data), UserAccountResponse::class.java)
        assertEquals(user.id, resultUser.id)
        assertEquals(user.fullName, resultUser.name)
        assertEquals("/pictures/testAvatar", resultUser.avatar)
        assertNotSame(user.changed, resultUser.changed)
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
    }

    private fun insertUser(): UserAccount =
        userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                email = email,
                password = password
            )
        )
}
