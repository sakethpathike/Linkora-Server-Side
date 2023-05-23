package com.sakethh.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class AppInfoDTO(
    val httpStatusCodeFromServer: String?,

    val latestVersion: String?,
    val latestVersionReleaseURL: String?,
    val changeLogForLatestVersion: String?,

    val latestStableVersion: String?,
    val latestStableVersionReleaseURL: String?,
    val changeLogForLatestStableVersion: String?
)