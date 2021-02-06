package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SubGroupService(
    private val subGroupRepository: SubGroupRepository,
    private val userAccountService: UserAccountService,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository
) {

    @Value(value = "\${brn.picture.theme.path}")
    private lateinit var pictureTheme: String

    private val log = logger()

    fun findSubGroupsForSeries(seriesId: Long): List<SubGroupDto> {
        log.debug("Try to find subGroups for seriesId=$seriesId")
        val subGroups = subGroupRepository.findBySeriesId(seriesId)
        return subGroups.map { subGroup -> subGroup.toDto(pictureTheme) }
    }

    fun findById(subGroupId: Long): SubGroupDto {
        log.debug("try to find SubGroup by Id=$subGroupId")
        val subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("No subGroup was found by id=$subGroupId.") }
        return subGroup.toDto(pictureTheme)
    }

    fun getSubGroupsProgressForUser(subGroupIds: List<Long>): Map<Long, Map<Int, Int>> {
        log.debug("Trying to get done/all exercises for each subGroup. subGroupIds=$subGroupIds")
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return subGroupIds.map {
            it to mapOf(
                studyHistoryRepository.getDoneExercises(it, currentUser.id!!).size to
                    exerciseRepository.findExercisesBySubGroupId(it).size
            )
        }.toMap()
    }
}

fun SubGroup.toDto(pictureUrlTemplate: String): SubGroupDto {
    val dto = this.toDto()
    val url = String.format(pictureUrlTemplate, dto.pictureUrl)
    dto.pictureUrl = url
    return dto
}
