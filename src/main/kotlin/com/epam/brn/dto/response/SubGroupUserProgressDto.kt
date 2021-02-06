package com.epam.brn.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 *@author Nikolai Lazarev
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class SubGroupUserProgressDto(
    val subGroupId: Long?,
    val done: Int = 0,
    val all: Int = 0
)
