package ua.com.radiokot.flybys.api

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskRequestBody
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskResponseBody
import ua.com.radiokot.flybys.api.model.TaskByIdResponseBody
import ua.com.radiokot.flybys.api.model.TaskStateResponse
import ua.com.radiokot.flybys.worker.FlybyAnalysisWorker

class FlybyAnalysisTasksApiController(
        private val flybyAnalysisWorker: FlybyAnalysisWorker
) {
    fun getById(ctx: Context) {
        val taskId = ctx.pathParam("id")

        val state = flybyAnalysisWorker.getState(taskId)
                ?: throw NotFoundResponse("Task $taskId not found")

        ctx.status(200)
        ctx.json(TaskByIdResponseBody(
                id = taskId,
                state = TaskStateResponse.fromActualState(state)
        ))
    }

    fun schedule(ctx: Context) {
        val bodyValidator = ctx.bodyValidator<ScheduleFlybyTaskRequestBody>()
        bodyValidator.check(
                { it.activityId.toLongOrNull() != null },
                "Invalid activity ID format"
        )
        val body = bodyValidator.get()

        val currentActiveTask =
                flybyAnalysisWorker.getTasksByActivityId(
                        activityId = body.activityId
                )
                        .find { !it.state.isFinal }

        // Do not schedule another task for activity
        // if there is already an active one.
        val taskId = currentActiveTask?.id
                ?: flybyAnalysisWorker.schedule(
                        activityId = body.activityId
                )

        ctx.status(201)
        ctx.json(ScheduleFlybyTaskResponseBody(
                taskId = taskId
        ))
    }
}