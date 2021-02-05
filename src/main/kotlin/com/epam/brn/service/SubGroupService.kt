package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.SubGroup
import com.epam.brn.model.SubGroupUserProgress
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap

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

    fun getSubGroupsProgressForUser(subGroupIds: List<Long>): Map<Long,SubGroupUserProgress> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        val subGroupIdToSubGroupProgressPair: Map<Long, SubGroupUserProgress> = emptyMap()
        subGroupIds.forEach {
            subGroupIdToSubGroupProgressPair + Pair(it, SubGroupUserProgress(
                    studyHistoryRepository.getDoneExercises(it, currentUser.id!!).size,
                    exerciseRepository.findExercisesBySubGroupId(it).size
            ))
        }
        return subGroupIdToSubGroupProgressPair
    }

}

fun SubGroup.toDto(pictureUrlTemplate: String): SubGroupDto {
    val dto = this.toDto()
    val url = String.format(pictureUrlTemplate, dto.pictureUrl)
    dto.pictureUrl = url
    return dto
}
