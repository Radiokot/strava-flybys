package ua.com.radiokot.flybys

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import ua.com.radiokot.flybys.api.FlybyAnalysisTasksApiController

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        BasicConfigurator.configure()
        Logger.getRootLogger().level = Level.INFO

        val app = Javalin.create { config ->
            config.showJavalinBanner = false
        }

        app.routes {
            path("api/tasks") {
                val controller = FlybyAnalysisTasksApiController()

                get(":id", controller::getById)
                post(controller::schedule)
            }
        }

        app.start(DEFAULT_PORT)
    }

    private const val DEFAULT_PORT = 8191
}