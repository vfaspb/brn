package com.epam.brn.integration

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class SubGroupControllerIT : BaseIT() {

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    private val baseUrl = "/subgroups"

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        exerciseRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    private fun insertUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                email = "test@test.test",
                password = "password",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                fullName = "testUser"
            )
        )
    }

    private fun insertStudyHistory(userAccount: UserAccount, exercise: Exercise): StudyHistory {
        return studyHistoryRepository.save(
            StudyHistory(
                userAccount = userAccount,
                exercise = exercise,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now(),
                executionSeconds = 1,
                replaysCount = 1,
                wrongAnswers = 0,
                tasksCount = 10
            )
        )
    }

    private fun insertExercise(subGroup: SubGroup): Exercise {
        return exerciseRepository.save(
            Exercise(
                name = "test${subGroup.id}",
                subGroup = subGroup
            )
        )
    }

    private fun insertSeries(): Series {
        val group = exerciseGroupRepository.save(
            ExerciseGroup(
                description = "desc",
                name = "group ExercisesControllerIT"
            )
        )
        return seriesRepository.save(
            Series(
                name = "series for SubGroupControllerIT",
                exerciseGroup = group,
                type = "type",
                level = 1
            )
        )
    }

    private fun insertSubGroup(series: Series, level: Int): SubGroup = subGroupRepository.save(
        SubGroup(series = series, level = level, code = "code", name = "subGroupName$level")
    )

    @Test
    fun `test get subGroups for series`() {
        // GIVEN
        val series = insertSeries()
        insertSubGroup(series, 1)
        insertSubGroup(series, 2)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("seriesId", series.id!!.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains("subGroupName1"))
        Assertions.assertTrue(response.contains("subGroupName2"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get subGroup for subGroupId`() {
        // GIVEN
        val series = insertSeries()
        val subGroup = insertSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains("subGroupName1"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `should return user progress for subGroup`() {

        val currentUser = insertUser()
        val series = insertSeries()
        val subGroups = listOf(
            insertSubGroup(series, 1),
            insertSubGroup(series, 10)
        )
        val exercises = listOf(
            insertExercise(subGroup = subGroups[0]),
            insertExercise(subGroup = subGroups[1])
        )
        insertStudyHistory(currentUser, exercises.first())

        val resultAction = mockMvc.perform(
            get("$baseUrl/statistic")
                .param("ids", "1,2")
        )

        val response = resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val baseResponseDto = objectMapper.readValue(response, BaseSingleObjectResponseDto::class.java)
        val baseResponseJson = Gson().toJson(baseResponseDto.data)
        val resultStatistic: Map<Long, Map<Int, Int>> =
            objectMapper.readValue(baseResponseJson, object : TypeReference<Map<Long, Map<Int, Int>>>() {})
        val values = resultStatistic.values

        Assertions.assertTrue(values.contains(mapOf(1 to 1)))
        Assertions.assertTrue(values.contains(mapOf(0 to 1)))
    }
}
