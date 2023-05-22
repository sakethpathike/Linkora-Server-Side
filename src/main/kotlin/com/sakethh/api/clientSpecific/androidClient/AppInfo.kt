package com.sakethh.api.clientSpecific.androidClient

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class AppInfo(val version: String)

fun Routing.appInfo() {
    get("/api/read/appInfo") {
        val currentVersion = com.sakethh.data.clientSpecific.androidClient.AppInfo().getCurrentStableVersion()
        val appInfo = AppInfo(version = currentVersion ?: "invalid redis url:(")
        call.respond(
            status = if (currentVersion != null) HttpStatusCode.OK else HttpStatusCode.InternalServerError,
            message = appInfo
        )
    }

    post("/api/write/appInfo") {
        val receivedData = call.receive<AppInfo>()
        val isDataUpdated = com.sakethh.data.clientSpecific.androidClient.AppInfo()
            .changeCurrentStableVersion(newVersion = receivedData.version)
        val httpCode: HttpStatusCode = when (isDataUpdated) {
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.VALUE_CHANGED -> HttpStatusCode.OK
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.VALUE_DID_NOT_CHANGED -> HttpStatusCode.BadRequest
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.REDIS_URL_INVALID -> HttpStatusCode.InternalServerError
        }
        val msg: String = when (isDataUpdated) {
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.VALUE_CHANGED -> "value has been changed."
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.VALUE_DID_NOT_CHANGED -> "something went wrong:("
            com.sakethh.data.clientSpecific.androidClient.AppInfo.StateOfRequest.REDIS_URL_INVALID -> "invalid redis url:("
        }
        call.respond(status = httpCode, message = msg)
    }
}