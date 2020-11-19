package ua.com.radiokot.flybys.pages

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker
import ua.com.radiokot.flybys.worker.model.FlybyAnalysisTask

class TaskMapByIdPageRenderer(
        private val flybyAnalysisWorker: FlybyAnalysisWorker,
        private val googleApiKey: String,
): PageRenderer {
    override fun render(ctx: Context) {
        val taskId = ctx.pathParam("id")

        val state = flybyAnalysisWorker.getState(taskId)
                ?: throw NotFoundResponse("Task $taskId not found")

        if (state !is FlybyAnalysisTask.State.Done) {
            throw BadRequestResponse("Task $taskId is not done yet")
        }

        ctx.render("map.html", mapOf(
                "taskId" to taskId,
                "googleApiKey" to googleApiKey,
                "sourceActivityName" to state.sourceActivity.name,
                "flybysCount" to state.flybys.size,
        ))
    }
}