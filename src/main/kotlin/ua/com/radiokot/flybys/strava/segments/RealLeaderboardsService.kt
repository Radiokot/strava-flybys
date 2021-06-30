package ua.com.radiokot.flybys.strava.segments

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import ua.com.radiokot.flybys.strava.http.FakeHeaders
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import ua.com.radiokot.flybys.strava.segments.model.LeaderboardResult
import ua.com.radiokot.flybys.strava.session.StravaSession
import java.util.logging.Level
import java.util.logging.Logger

class RealLeaderboardsService(
        private val session: StravaSession,
        private val httpClient: OkHttpClient,
) : LeaderboardsService {
    private val objectMapper = ObjectMapper()

    override fun getTodaySegmentLeaderboard(segmentId: String): List<LeaderboardResult> {
        Logger.getGlobal().log(Level.INFO, "Loading $segmentId today leaderboard...")

        val nowTime = System.currentTimeMillis()
        val request = Request.Builder()
                .get()
                .url("${session.stravaRootUrl}/segments/$segmentId/leaderboard" +
                        "?raw=true&page=1&per_page=50&filter=overall&date_range=today&_=$nowTime")
                .addHeaders(session.getHeaders().toHeaders())
                .addHeaders(FakeHeaders.extraForJsonResponse.toHeaders())
                .build()

        RequestRateLimiter.waitBeforeRequest()
        val response = httpClient.newCall(request).execute()

        val rawJson = objectMapper.readTree(response.body!!.charStream())
        response.body?.closeQuietly()
        val topResults = rawJson["top_results"]

        return topResults
                .map(::LeaderboardResult)
    }
}