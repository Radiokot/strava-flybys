package ua.com.radiokot.flybys.api

import io.javalin.http.Context
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskRequestBody
import ua.com.radiokot.flybys.api.model.ScheduleFlybyTaskResponseBody

class FlybyAnalysisTasksApiController {
    fun getById(ctx: Context) {

    }

    fun schedule(ctx: Context) {
        val bodyValidator = ctx.bodyValidator<ScheduleFlybyTaskRequestBody>()
        bodyValidator.check(
                { it.activityId.toLongOrNull() != null },
                "Invalid activity ID format"
        )
        val body = bodyValidator.get()

        ctx.status(201)
        ctx.json(ScheduleFlybyTaskResponseBody(
                taskId = body.activityId.hashCode().toString()
        ))
    }
}