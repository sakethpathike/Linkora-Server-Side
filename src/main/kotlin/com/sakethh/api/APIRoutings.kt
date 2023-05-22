package com.sakethh.api

import com.sakethh.api.clientSpecific.androidClient.appInfo
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.apiRoutings() {
    routing {
        appInfo()
    }
}