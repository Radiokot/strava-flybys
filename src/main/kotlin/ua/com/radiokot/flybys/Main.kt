package ua.com.radiokot.flybys

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.staticfiles.Location
import sun.misc.Signal
import ua.com.radiokot.flybys.analysis.FlybyAnalysis
import ua.com.radiokot.flybys.api.FlybyAnalysisTasksApiController
import ua.com.radiokot.flybys.pages.IndexPageRenderer
import ua.com.radiokot.flybys.pages.TaskByIdPageRenderer
import ua.com.radiokot.flybys.pages.TaskMapByIdPageRenderer
import ua.com.radiokot.flybys.strava.activities.RealActivitiesService
import ua.com.radiokot.flybys.strava.http.HttpClientFactory
import ua.com.radiokot.flybys.strava.segments.RealLeaderboardsService
import ua.com.radiokot.flybys.strava.session.RealStravaSession
import ua.com.radiokot.flybys.strava.streams.RealStreamsService
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
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

        val googleApiKey = System.getenv("GOOGLE_API_KEY")
            ?: ""

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
                        flybyAnalysisWorker = flybyAnalysisWorker,
                    )

                    get(":id", controller::getById)
                    post(controller::schedule)
                }


                get("/", IndexPageRenderer()::render)
                path("/tasks") {
                    get(
                        ":id", TaskByIdPageRenderer(
                            flybyAnalysisWorker = flybyAnalysisWorker,
                        )::render
                    )
                    get(
                        ":id/map", TaskMapByIdPageRenderer(
                            flybyAnalysisWorker = flybyAnalysisWorker,
                            googleApiKey = googleApiKey,
                        )::render
                    )
                }
            }
            .start(port)
            .apply {
                // Gracefully stop on SIGINT and SIGTERM.
                listOf("INT", "TERM").forEach {
                    Signal.handle(Signal(it)) {
                        stop()
                        flybyAnalysisWorker.shutdownNow()
                    }
                }
            }
    }

    private const val DEFAULT_PORT = 8191
    private const val DEFAULT_STRAVA_ROOT_URL = "https://www.strava.com"
    private const val DEFAULT_STRAVA_HOSTNAME = "www.strava.com"
}