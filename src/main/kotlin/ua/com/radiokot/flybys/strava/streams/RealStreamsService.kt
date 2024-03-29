package ua.com.radiokot.flybys.strava.streams

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import ua.com.radiokot.flybys.strava.http.FakeHeaders
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import ua.com.radiokot.flybys.strava.http.addHeaders
import ua.com.radiokot.flybys.strava.session.StravaSession
import ua.com.radiokot.flybys.strava.streams.exceptions.StreamNotFoundException
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint
import java.net.HttpURLConnection

class RealStreamsService(
    private val session: StravaSession,
    private val httpClient: OkHttpClient,
) : StreamsService {
    private val objectMapper = ObjectMapper()
    private val logger = KotlinLogging.logger("RealStreamsService")

    override fun getActivityLocationTimeStream(activityId: String): List<LocationTimePoint> {
        logger.debug {
            "Getting activity $activityId location-time stream..."
        }

        val request = Request.Builder()
            .get()
            .url(
                "${session.stravaRootUrl}/activities/$activityId/streams" +
                        "?stream_types[]=latlng&stream_types[]=time"
            )
            .addHeaders(session.getHeaders().toHeaders())
            .addHeaders(FakeHeaders.extraForJsonResponse.toHeaders())
            .build()

        RequestRateLimiter.waitBeforeRequest()
        val response = httpClient.newCall(request).execute()

        if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
            throw StreamNotFoundException(activityId)
        }

        val rawJson = objectMapper.readTree(response.body!!.charStream())
        response.body?.closeQuietly()
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