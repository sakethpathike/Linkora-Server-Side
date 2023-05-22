package com.sakethh.plugins

import com.sakethh.api.apiRoutings
import io.ktor.server.application.*

fun Application.configureRouting() {
    apiRoutings()
}
