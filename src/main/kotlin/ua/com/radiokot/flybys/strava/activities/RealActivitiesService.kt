package ua.com.radiokot.flybys.strava.activities

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.athletes.model.Athlete
import ua.com.radiokot.flybys.strava.http.FakeHeaders
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import ua.com.radiokot.flybys.strava.http.addHeaders
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import ua.com.radiokot.flybys.strava.session.StravaSession
import ua.com.radiokot.flybys.strava.streams.StreamsService

class RealActivitiesService(
    private val session: StravaSession,
    private val httpClient: OkHttpClient,
    private val streamsService: StreamsService,
) : ActivitiesService {
    private val objectMapper = ObjectMapper()
    private val logger = KotlinLogging.logger("RealActivitiesService")

    override fun getById(
        activityId: String,
        includeSegmentEfforts: Boolean
    ): Activity {
        logger.debug {
            "Getting activity $activityId..."
        }

        val stream = streamsService.getActivityLocationTimeStream(activityId)

        val request = Request.Builder()
            .get()
            .url("${session.stravaRootUrl}/activities/${activityId}/overview")
            .addHeaders(session.getHeaders().toHeaders())
            .addHeaders(FakeHeaders.extraForHtmlResponse.toHeaders())
            .build()

        RequestRateLimiter.waitBeforeRequest()
        val response = httpClient.newCall(request).execute()
        val rawHtml = response.body!!.string()

        val startDateLocalRegex = "startDateLocal:\\s?(\\d+?),".toRegex()
        val startedAtLocal = startDateLocalRegex.find(rawHtml)
            ?.groupValues
            ?.get(1)
            ?.let {
                it.toLongOrNull()
                    ?: throw IllegalStateException("Start date found but it's not a number")
            }
            ?: throw IllegalStateException("No matches for start date in activity HTML")

        val athleteJsonRegex =
            "activityAthlete\\s?=\\s?new Strava\\.Models\\.Athlete\\((.+?)\\);".toRegex()
        val athleteJson = athleteJsonRegex.find(rawHtml)
            ?.groupValues
            ?.get(1)
            ?.let(objectMapper::readTree)
            ?: throw IllegalStateException("No matches for athlete JSON in activity HTML")

        val segmentEffortsRegex =
            "pageView.segmentEfforts\\(\\)\\.reset\\((.+?),\\s?\\{\\s?parse".toRegex()
        val segmentEfforts =
            if (includeSegmentEfforts) {
                val segmentEffortsJson = segmentEffortsRegex.find(rawHtml)
                    ?.groupValues
                    ?.get(1)
                    ?.let(objectMapper::readTree)
                    ?: throw IllegalStateException("No matches for segment efforts JSON in activity HTML")

                segmentEffortsJson["efforts"]
                    .map(::SegmentEffort)
            } else {
                emptyList()
            }

        val nameRegex = "activity-name'>(.+?)\\s?<".toRegex()
        var name = "---"
        try {
            name = nameRegex.find(rawHtml)!!
                .groupValues[1]
        } catch (_: Exception) {
            logger.warn {
                "Unable to get name from activity $activityId HTML"
            }
        }

        return Activity(
            id = activityId,
            name = name,
            startedAtLocal = startedAtLocal,
            endedAtLocal = stream.last().getAbsoluteTime(startedAtLocal),
            locationTimeStream = stream,
            athlete = Athlete(athleteJson),
            segmentEfforts = segmentEfforts
        )
    }
}