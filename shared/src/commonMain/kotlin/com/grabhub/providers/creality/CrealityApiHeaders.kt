package com.grabhub.providers.creality

import kotlin.random.Random

internal object CrealityApiHeaders {
    const val BASE_URL = "https://model-admin2.creality.com"
    const val WEB_BASE_URL = "https://www.crealitycloud.com"

    fun build(deviceId: String = randomDeviceId()): Map<String, String> = mapOf(
        "Content-Type" to "application/json; charset=UTF-8",
        "__CXY_APP_ID_" to "creality_model",
        "__CXY_OS_LANG_" to "0",
        "__CXY_DUID_" to deviceId,
        "__CXY_OS_VER_" to "14",
        "__CXY_PLATFORM_" to "14",
        "__CXY_REQUESTID_" to randomRequestId(),
    )

    private fun randomDeviceId(): String =
        Random.nextBytes(6).joinToString("") { byte ->
            (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
        }

    private fun randomRequestId(): String =
        buildString(32) {
            val chars = "0123456789abcdef"
            repeat(32) { append(chars[Random.nextInt(chars.length)]) }
        }
}
