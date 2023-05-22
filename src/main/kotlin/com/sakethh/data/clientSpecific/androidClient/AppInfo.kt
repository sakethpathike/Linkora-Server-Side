package com.sakethh.data.clientSpecific.androidClient

import redis.clients.jedis.Jedis
import redis.clients.jedis.util.JedisURIHelper
import java.net.URI

class AppInfo {
    fun getCurrentStableVersion(): String? {
        val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
        return if (isThisAValidRedisURL) {
            val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
            redisConnection.use { connection ->
                connection.get(System.getenv("STABLE_VERSION_KEY_NAME").toString())
            }
        } else {
            null
        }
    }

    enum class StateOfRequest {
        REDIS_URL_INVALID, VALUE_DID_NOT_CHANGED, VALUE_CHANGED
    }

    fun changeCurrentStableVersion(newVersion: String): StateOfRequest {
        val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
        return if (isThisAValidRedisURL) {
            val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
            val nonUpdatedVersion = redisConnection.get(System.getenv("STABLE_VERSION_KEY_NAME").toString())
            redisConnection.set(nonUpdatedVersion, newVersion)
            redisConnection.use { connection ->
                if (connection.get(System.getenv("STABLE_VERSION_KEY_NAME").toString()) == newVersion) {
                    StateOfRequest.VALUE_CHANGED
                } else {
                    StateOfRequest.VALUE_DID_NOT_CHANGED
                }
            }
        } else {
            StateOfRequest.REDIS_URL_INVALID
        }
    }
}