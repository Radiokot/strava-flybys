package ua.com.radiokot.flybys.api

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskRequestBody
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskResponseBody
import ua.com.radiokot.flybys.api.model.TaskByIdResponseBody
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker

class FlybyAnalysisTasksApiController(
        private val flybyAnalysisWorker: FlybyAnalysisWorker
) {
    fun getById(ctx: Context) {
        val taskId = ctx.pathParam("id")

        val state = flybyAnalysisWorker.getState(taskId)
                ?: throw NotFoundResponse("Task not found")

        ctx.status(200)
        ctx.json(TaskByIdResponseBody(
                id = taskId,
                state = TaskByIdResponseBody.State.fromActualState(state)
        ))
    }

    fun schedule(ctx: Context) {
        val bodyValidator = ctx.bodyValidator<ScheduleFlybyTaskRequestBody>()
        bodyValidator.check(
                { it.activityId.toLongOrNull() != null },
                "Invalid activity ID format"
        )
        val body = bodyValidator.get()

        val taskId = flybyAnalysisWorker.schedule(
                activityId = body.activityId
        )

        ctx.status(201)
        ctx.json(ScheduleFlybyTaskResponseBody(
                taskId = taskId
        ))
    }
}