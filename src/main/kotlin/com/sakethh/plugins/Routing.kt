package com.sakethh.plugins

import com.sakethh.api.apiRoutings
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routings() {
    routing {
        get("/") {
            call.respondRedirect(url = System.getenv("DEFAULT_ROUTE_REDIRECT_URL"))
        }
    }
    apiRoutings()
}
