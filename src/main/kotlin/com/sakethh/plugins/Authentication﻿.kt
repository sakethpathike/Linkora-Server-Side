package com.sakethh.plugins

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.JedisURIHelper
import java.net.URI

enum class AuthEnums {
    POST_DATA_TO_REDIS_UTILS, GET_DATA_FROM_REDIS_UTILS
}

fun Application.authentication() {
    @Serializable
    data class CredentialsFromRedis(val name: String, val password: String)

    install(Authentication) {
        basic(name = AuthEnums.POST_DATA_TO_REDIS_UTILS.name) {
            validate { credentials ->
                val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
                if (isThisAValidRedisURL) {
                    val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
                    val rawCredentialsFromRedis = redisConnection.use {
                        it.get(System.getenv("POST_AUTH_KEY_NAME")).toString()
                    }
                    val decodedCredentialsFromRedis =
                        Gson().fromJson(rawCredentialsFromRedis, CredentialsFromRedis::class.java)
                    if (credentials.name == decodedCredentialsFromRedis.name && credentials.password == decodedCredentialsFromRedis.password) {
                        UserIdPrincipal(name = "admin_In_da_Houz")
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
        bearer(name = AuthEnums.GET_DATA_FROM_REDIS_UTILS.name) {
            authenticate {headerData->
                val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
                if (isThisAValidRedisURL) {
                    val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
                    val rawCredentialsFromRedis = redisConnection.use {redisInstance->
                        redisInstance.get(System.getenv("READ_AUTH_KEY_NAME"))
                    }
                    if (headerData.token == rawCredentialsFromRedis) {
                        UserIdPrincipal("admin_In_da_Houz")
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }
}