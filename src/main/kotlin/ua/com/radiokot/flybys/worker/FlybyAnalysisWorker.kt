package ua.com.radiokot.flybys.worker

import ua.com.radiokot.flybys.analysis.FlybyAnalysis
import ua.com.radiokot.flybys.strava.activities.ActivitiesService
import ua.com.radiokot.flybys.worker.model.FlybyAnalysisTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Processes Flyby analysis tasks and manages their states.
 */
class FlybyAnalysisWorker(
        private val activitiesService: ActivitiesService,
        private val flybyAnalysis: FlybyAnalysis,
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val cleanUpExecutor = Executors.newSingleThreadScheduledExecutor()

    private val tasks = mutableMapOf<String, FlybyAnalysisTask>()

    init {
        cleanUpExecutor.scheduleAtFixedRate(
                this::cleanUpFinalizedTasks,
                1,
                1,
                TimeUnit.HOURS
        )

        Logger.getGlobal().log(Level.INFO, "Worker is ready")
    }

    /**
     * Schedules Flyby analysis task for activity with given [activityId]
     *
     * @return task id
     */
    fun schedule(activityId: String): String {
        val task = FlybyAnalysisTask(
                activityId = activityId
        )

        tasks[task.id] = task

        Logger.getGlobal().log(Level.INFO, "Scheduled ${task.id}, now ${tasks.size} in queue")

        executor.submit { processTask(task) }

        return task.id
    }

    /**
     * @return task state or null if there is no such task
     */
    fun getState(taskId: String): FlybyAnalysisTask.State? {
        return tasks[taskId]?.state
    }

    private fun processTask(task: FlybyAnalysisTask) {
        task.state = FlybyAnalysisTask.State.InProgress

        Logger.getGlobal().log(Level.INFO, "Task ${task.id} is in progress")

        try {
            val activity = activitiesService.getById(task.activityId)
            val flybys = flybyAnalysis.getActivityFlybys(activity)
            task.state = FlybyAnalysisTask.State.Done(
                    sourceActivity = activity,
                    flybys = flybys
            )

            Logger.getGlobal().log(Level.INFO, "Task ${task.id} is done")
        } catch (e: Exception) {
            task.state = FlybyAnalysisTask.State.Failed(e)
            Logger.getGlobal().log(Level.INFO, "Task ${task.id} is failed: $e")
        }
    }

    private fun cleanUpFinalizedTasks() {
        val toRemove = tasks
                .filterValues { task ->
                    task.state.isFinal &&
                            System.currentTimeMillis() / 1000 - task.createdAt > FINALIZED_TASK_STORAGE_DURATION_S
                }

        if (toRemove.isEmpty()) {
            toRemove.keys.forEach(tasks::remove)
            Logger.getGlobal().log(Level.INFO, "Cleaned up ${toRemove.size} finalized tasks")
        }
    }

    private companion object {
        private const val FINALIZED_TASK_STORAGE_DURATION_S = 5 * 23 * 3600
    }
}