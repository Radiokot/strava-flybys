package ua.com.radiokot.flybys.strava.session

import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ua.com.radiokot.flybys.strava.http.FakeHeaders
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import ua.com.radiokot.flybys.strava.http.addHeaders
import java.net.HttpURLConnection
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class RealStravaSession(
        override val email: String,
        private val password: String,
        override val stravaRootUrl: String,
        private val httpClient: OkHttpClient,
) : StravaSession {
    private var currentHeaders: Map<String, String>? = null
    private var currentHeadersObtainedAt: Date = Date(0)

    private val isExpired: Boolean
        get() = currentHeadersObtainedAt.time + LIFETIME_MS < System.currentTimeMillis()

    override fun getHeaders(): Map<String, String> = synchronized(this) {
        val currentHeaders = this.currentHeaders

        return@synchronized if (currentHeaders != null && !isExpired)
            currentHeaders
        else
            getNewHeaders()
                    .also { newHeaders ->
                        this.currentHeaders = newHeaders
                        currentHeadersObtainedAt = Date()
                    }
    }

    private fun getNewHeaders(): Map<String, String> {
        val loginParams = getLoginParams()

        val headers = mapOf(
                "content-type" to "application/x-www-form-urlencoded",
                "cookie" to loginParams.cookie,
                "referer" to "$stravaRootUrl/login"
        )

        Logger.getGlobal().log(Level.INFO, "Performing login...")

        val request = Request.Builder()
                .post(FormBody.Builder(Charsets.UTF_8)
                        .add("authenticity_token", loginParams.authenticityToken)
                        .add("plan", "")
                        .add("email", email)
                        .add("password", password)
                        .build()
                )
                .url("$stravaRootUrl/session")
                .addHeaders(headers.toHeaders())
                .addHeaders(FakeHeaders.extraForHtmlResponse.toHeaders())
                .build()

        RequestRateLimiter.awaitForRequest()
        val response = httpClient.newCall(request).execute()

        val responseCode = response.code
        check(responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            "Unexpected session response code $responseCode"
        }

        val redirectLocation = response.header("location")
        check(redirectLocation?.contains("dashboard") == true) {
            "Unexpected session response redirect location $redirectLocation"
        }

        val cookie = getCookieContentFromResponse(response)

        return mapOf(
                "cookie" to cookie
        )
    }

    data class LoginParams(
            val cookie: String,
            val authenticityToken: String,
    )

    private fun getLoginParams(): LoginParams {
        Logger.getGlobal().log(Level.INFO, "Obtaining login params...")

        val request = Request.Builder()
                .get()
                .url("$stravaRootUrl/login")
                .addHeaders(FakeHeaders.extraForHtmlResponse.toHeaders())
                .build()

        RequestRateLimiter.awaitForRequest()
        val response = httpClient.newCall(request).execute()
        val rawHtml = response.body!!.string()

        val crsfTokenRegex = "<meta name=\"csrf-token\" content=\"(.+?)\"".toRegex()
        val crsfToken = crsfTokenRegex
                .find(rawHtml)
                ?.groupValues
                ?.get(1)
                ?: throw IllegalStateException("No matches for crsf-token in login HTML")

        return LoginParams(
                cookie = getCookieContentFromResponse(response),
                authenticityToken = crsfToken
        )
    }

    private fun getCookieContentFromResponse(response: Response): String {
        val value = response.header("set-cookie")
                ?: throw IllegalStateException("There is no cookies in response header")

        return value.split(';')[0] + "; "
    }

    private companion object {
        private const val LIFETIME_MS = 3600_000
    }
}