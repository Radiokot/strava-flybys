package ua.com.radiokot.flybys.worker.model

import ua.com.radiokot.flybys.analysis.model.ActivityFlyby
import ua.com.radiokot.flybys.strava.activities.model.Activity
import java.util.*

data class FlybyAnalysisTask(
        val activityId: String,
        var state: State = State.Scheduled,
        /**
         * UNIX timestamp
         */
        val createdAt: Long = System.currentTimeMillis() / 1000,
        val id: String = UUID.randomUUID().toString(),
) {
    sealed class State(val isFinal: Boolean) {
        object Scheduled : State(isFinal = false)

        object InProgress : State(isFinal = false)

        class Failed(val cause: Throwable) : State(isFinal = true)

        class Done(val sourceActivity: Activity,
                   val flybys: List<ActivityFlyby>) : State(isFinal = true)
    }
}