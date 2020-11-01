package ua.com.radiokot.flybys.pages

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ua.com.radiokot.flybys.api.model.TaskStateResponse
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker

class TaskByIdPageRenderer(
        private val flybyAnalysisWorker: FlybyAnalysisWorker,
): PageRenderer {
    override fun render(ctx: Context) {
        val taskId = ctx.pathParam("id")

        val state = flybyAnalysisWorker.getState(taskId)
                ?: throw NotFoundResponse("Task $taskId not found")

        ctx.render("task.html", mapOf(
                "taskId" to taskId,
                "currentStateName" to TaskStateResponse.fromActualState(state).name
        ))
    }
}