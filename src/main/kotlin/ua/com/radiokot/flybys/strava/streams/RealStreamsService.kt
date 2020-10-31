package ua.com.radiokot.flybys.strava.streams

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.flybys.strava.http.FakeHeaders
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import ua.com.radiokot.flybys.strava.http.addHeaders
import ua.com.radiokot.flybys.strava.session.StravaSession
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint
import java.util.logging.Level
import java.util.logging.Logger

class RealStreamsService(
        private val session: StravaSession,
        private val httpClient: OkHttpClient,
): StreamsService {
    private val objectMapper = ObjectMapper()

    override fun getActivityLocationTimeStream(activityId: String): List<LocationTimePoint> {
        Logger.getGlobal().log(Level.INFO, "Getting activity $activityId location-time stream...")

        val request = Request.Builder()
                .get()
                .url("${session.stravaRootUrl}/activities/$activityId/streams" +
                        "?stream_types[]=latlng&stream_types[]=time")
                .addHeaders(session.getHeaders().toHeaders())
                .addHeaders(FakeHeaders.extraForJsonResponse.toHeaders())
                .build()

        RequestRateLimiter.awaitForRequest()
        val response = httpClient.newCall(request).execute()

        val rawJson = objectMapper.readTree(response.body!!.charStream())
        val latLngs = rawJson["latlng"]
        val timestamps = rawJson["time"]

        return latLngs.mapIndexed { i, latLng ->
            LocationTimePoint(
                    lat = latLng[0].asDouble(),
                    lng = latLng[1].asDouble(),
                    timeOffsetSeconds = timestamps[i].asInt()
            )
        }
    }
}