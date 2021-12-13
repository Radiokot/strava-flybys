package ua.com.radiokot.flybys.worker

import mu.KotlinLogging
import ua.com.radiokot.flybys.analysis.FlybyAnalysis
import ua.com.radiokot.flybys.strava.activities.ActivitiesService
import ua.com.radiokot.flybys.worker.model.FlybyAnalysisTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Processes Flyby analysis tasks and manages their states.
 */
class FlybyAnalysisWorker(
    private val activitiesService: ActivitiesService,
    private val flybyAnalysis: FlybyAnalysis,
) {
    private val logger = KotlinLogging.logger("FlybyAnalysisWorker")

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

        logger.debug { "Worker is ready" }
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

        logger.info { "Scheduled ${task.id}, now ${tasks.size} in queue" }

        executor.submit { processTask(task) }

        return task.id
    }

    /**
     * @return task state or null if there is no such task
     */
    fun getState(taskId: String): FlybyAnalysisTask.State? {
        return tasks[taskId]?.state
    }

    fun getTasksByActivityId(activityId: String): Collection<FlybyAnalysisTask> {
        return tasks.values.filter { it.activityId == activityId }
    }

    private fun processTask(task: FlybyAnalysisTask) {
        task.state = FlybyAnalysisTask.State.InProgress

        logger.debug { "Task ${task.id} is in progress" }

        try {
            val activity = activitiesService.getById(task.activityId)
            val flybys = flybyAnalysis.getActivityFlybys(activity)
            task.state = FlybyAnalysisTask.State.Done(
                sourceActivity = activity,
                flybys = flybys
            )

            logger.info { "Task ${task.id} is done" }
        } catch (e: Exception) {
            task.state = FlybyAnalysisTask.State.Failed(e)
            logger.warn { "Task ${task.id} is failed: $e" }
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
            logger.debug { "Cleaned up ${toRemove.size} finalized tasks" }
        }
    }

    fun shutdownNow() {
        logger.debug { "Shutdown now" }
        executor.shutdownNow()
        cleanUpExecutor.shutdownNow()
    }

    private companion object {
        private const val FINALIZED_TASK_STORAGE_DURATION_S = 5 * 23 * 3600
    }
}