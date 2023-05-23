package com.sakethh.data.clientSpecific.androidClient

import com.sakethh.dto.common.AppInfoDTO
import io.ktor.http.*
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.JedisURIHelper
import java.net.URI

class AppInfo {
    fun getAppInfoData(): AppInfoDTO {
        val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
        return if (isThisAValidRedisURL) {
            val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
            redisConnection.use { connection ->
                AppInfoDTO(
                    httpStatusCodeFromServer = HttpStatusCode.OK.toString(),
                    latestVersion = connection.get(System.getenv("LATEST_VERSION_KEY_NAME")),
                    latestVersionReleaseURL = connection.get(System.getenv("LATEST_VERSION_RELEASE_URL")),
                    changeLogForLatestVersion = connection.get(System.getenv("LATEST_VERSION_CHANGE_LOG")),
                    latestStableVersion = connection.get(System.getenv("LATEST_STABLE_VERSION_KEY_NAME")),
                    latestStableVersionReleaseURL = connection.get(System.getenv("LATEST_STABLE_VERSION_RELEASE_URL")),
                    changeLogForLatestStableVersion = connection.get(System.getenv("LATEST_STABLE_VERSION_CHANGE_LOG")),
                )
            }
        } else {
            AppInfoDTO(
                httpStatusCodeFromServer = HttpStatusCode.OK.toString(),
                latestVersion = null,
                latestVersionReleaseURL = null,
                changeLogForLatestVersion = null,
                latestStableVersion = null,
                latestStableVersionReleaseURL = null,
                changeLogForLatestStableVersion = null,
            )
        }
    }

    enum class StateOfRequest {
        REDIS_URL_INVALID, VALUE_DID_NOT_CHANGED, VALUE_CHANGED
    }

    enum class RedisKeyType {
        LATEST_VERSION_VALUE, LATEST_VERSION_CHANGELOG, LATEST_VERSION_RELEASE_URL, LATEST_STABLE_VERSION_VALUE, LATEST_STABLE_VERSION_CHANGELOG, LATEST_STABLE_VERSION_RELEASE_URL
    }

    fun changeAppInfoDataSpecifically(newValue: String, keyType: RedisKeyType): StateOfRequest {
        val isThisAValidRedisURL = JedisURIHelper.isValid(URI(System.getenv("REDIS_CONNECTION_URL").toString()))
        return if (isThisAValidRedisURL) {
            val redisConnection = Jedis(System.getenv("REDIS_CONNECTION_URL").toString())
            val keyName = when (keyType) {
                RedisKeyType.LATEST_VERSION_RELEASE_URL -> System.getenv("LATEST_VERSION_RELEASE_URL").toString()

                RedisKeyType.LATEST_VERSION_VALUE -> System.getenv("LATEST_VERSION_KEY_NAME").toString()

                RedisKeyType.LATEST_VERSION_CHANGELOG -> System.getenv("LATEST_VERSION_CHANGE_LOG")

                RedisKeyType.LATEST_STABLE_VERSION_VALUE -> System.getenv("LATEST_STABLE_VERSION_KEY_NAME")

                RedisKeyType.LATEST_STABLE_VERSION_CHANGELOG -> System.getenv("LATEST_STABLE_VERSION_CHANGE_LOG")
                    .toString()

                RedisKeyType.LATEST_STABLE_VERSION_RELEASE_URL -> System.getenv("LATEST_STABLE_VERSION_RELEASE_URL")
                    .toString()
            }
            redisConnection.set(keyName, newValue)
            redisConnection.use {
                if (it.get(keyName) == newValue) {
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