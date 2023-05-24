package com.sakethh.api.clientSpecific.androidClient

import com.sakethh.data.clientSpecific.androidClient.AppInfo
import com.sakethh.plugins.AuthEnums
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


fun Routing.appInfo() {

    authenticate(AuthEnums.GET_DATA_FROM_REDIS_UTILS.name) {
        get("/api/read/appInfo") {
            val appInfoDataFromRedis = AppInfo().getAppInfoData()
            call.respond(
                status = if (appInfoDataFromRedis.httpStatusCodeFromServer.toString() != HttpStatusCode.InternalServerError.toString()) HttpStatusCode.OK else HttpStatusCode.InternalServerError,
                message = appInfoDataFromRedis
            )
        }
    }

    @Serializable
    data class UpdateDataDTO(val newValue: String, val keyType: AppInfo.RedisKeyType)

    authenticate(AuthEnums.POST_DATA_TO_REDIS_UTILS.name) {
        post("/api/write/appInfo") {
            val receivedData = call.receive<UpdateDataDTO>()
            val isDataUpdated = AppInfo()
                .changeAppInfoDataSpecifically(newValue = receivedData.newValue, keyType = receivedData.keyType)
            val httpCode: HttpStatusCode = when (isDataUpdated) {
                AppInfo.StateOfRequest.VALUE_CHANGED -> HttpStatusCode.OK
                AppInfo.StateOfRequest.VALUE_DID_NOT_CHANGED -> HttpStatusCode.BadRequest
                AppInfo.StateOfRequest.REDIS_URL_INVALID -> HttpStatusCode.InternalServerError
            }
            val msg: String = when (isDataUpdated) {
                AppInfo.StateOfRequest.VALUE_CHANGED -> "value has been changed."
                AppInfo.StateOfRequest.VALUE_DID_NOT_CHANGED -> "something went wrong:("
                AppInfo.StateOfRequest.REDIS_URL_INVALID -> "invalid redis url:("
            }
            call.respond(status = httpCode, message = msg)
        }
    }
}