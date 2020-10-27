package ua.com.radiokot.flybys

import io.javalin.Javalin
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        BasicConfigurator.configure()
        Logger.getRootLogger().level = Level.INFO

        val app = Javalin
                .create { config ->
                    config.showJavalinBanner = false
                }
                .start(DEFAULT_PORT)

        app.get("/") { ctx ->
            ctx.json(mapOf(
                    "result" to "ok",
                    "thread" to Thread.currentThread().name
            ))
        }
    }

    private const val DEFAULT_PORT = 8191
}