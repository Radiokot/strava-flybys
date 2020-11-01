package ua.com.radiokot.flybys

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.staticfiles.Location
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import ua.com.radiokot.flybys.analysis.FlybyAnalysis
import ua.com.radiokot.flybys.api.FlybyAnalysisTasksApiController
import ua.com.radiokot.flybys.strava.activities.RealActivitiesService
import ua.com.radiokot.flybys.strava.http.HttpClientFactory
import ua.com.radiokot.flybys.strava.segments.RealLeaderboardsService
import ua.com.radiokot.flybys.strava.session.RealStravaSession
import ua.com.radiokot.flybys.strava.streams.RealStreamsService
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker
import java.text.SimpleDateFormat
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        BasicConfigurator.configure()
        Logger.getRootLogger().level = Level.INFO

        val port = System.getenv("PORT")?.toIntOrNull()
                ?: DEFAULT_PORT

        val stravaRootUrl = System.getenv("STRAVA_ROOT_URL")
                ?: DEFAULT_STRAVA_ROOT_URL
        val stravaHostname = System.getenv("STRAVA_HOSTNAME")
                ?: DEFAULT_STRAVA_HOSTNAME

        val email = System.getenv("EMAIL")
                ?: throw IllegalArgumentException("No email env variable specified")
        val password = System.getenv("PASSWORD")
                ?: throw IllegalArgumentException("No password env variable specified")

        val httpClientFactory = HttpClientFactory(
                stravaRootUrl = stravaRootUrl,
                stravaHostname = stravaHostname,
                withLogs = true
        )
        val stravaSession = RealStravaSession(
                email = email,
                password = password,
                stravaRootUrl = stravaRootUrl,
                httpClient = httpClientFactory.httpClient
        )
        val streamsService = RealStreamsService(
                session = stravaSession,
                httpClient = httpClientFactory.httpClient
        )
        val activitiesService = RealActivitiesService(
                session = stravaSession,
                httpClient = httpClientFactory.httpClient,
                streamsService = streamsService
        )
        val leaderboardsService = RealLeaderboardsService(
                session = stravaSession,
                httpClient = httpClientFactory.httpClient
        )
        val flybyAnalysis = FlybyAnalysis(
                activitiesService = activitiesService,
                leaderboardsService = leaderboardsService
        )
        val flybyAnalysisWorker = FlybyAnalysisWorker(
                activitiesService = activitiesService,
                flybyAnalysis = flybyAnalysis
        )

        Javalin
                .create { config ->
                    config.showJavalinBanner = false
                    config.addStaticFiles("/static", "/static", Location.CLASSPATH)
                }
                .routes {
                    path("api/tasks") {
                        val controller = FlybyAnalysisTasksApiController(
                                flybyAnalysisWorker = flybyAnalysisWorker
                        )

                        get(":id", controller::getById)
                        post(controller::schedule)
                    }

                    path ("/") {
                        get { ctx ->
                            ctx.render("index.html", mapOf(
                                    "date" to SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Date())
                            ))
                        }
                    }
                }
                .start(port)
    }

    private const val DEFAULT_PORT = 8191
    private const val DEFAULT_STRAVA_ROOT_URL = "https://www.strava.com"
    private const val DEFAULT_STRAVA_HOSTNAME = "www.strava.com"
}