package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.flybys.worker.model.FlybyAnalysisTask

class TaskByIdResponseBody(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("state")
        val state: State,
) {
    sealed class State(
            @JsonProperty("name")
            val name: String,
    ) {
        object Scheduled: State("scheduled")

        object InProgress: State("in_progress")

        class Failed(
                @JsonProperty("cause")
                val cause: String,
                @JsonProperty("message")
                val message: String,
        ): State("failed")

        class Done(
                @JsonProperty("activity")
                val activity: ActivityResponse,
                @JsonProperty("flybys")
                val flybys: List<ActivityFlybyResponse>,
        ): State("done")

        companion object {
            fun fromActualState(state: FlybyAnalysisTask.State): State = when (state) {
                FlybyAnalysisTask.State.Scheduled ->
                    Scheduled
                FlybyAnalysisTask.State.InProgress ->
                    InProgress
                is FlybyAnalysisTask.State.Failed ->
                    Failed(
                            cause = state.cause::class.java.simpleName,
                            message = state.cause.localizedMessage
                    )
                is FlybyAnalysisTask.State.Done ->
                    Done(
                            activity = ActivityResponse(state.sourceActivity),
                            flybys = state.flybys.map(::ActivityFlybyResponse)
                    )
            }
        }
    }
}