package ua.com.radiokot.flybys.analysis

import ua.com.radiokot.flybys.analysis.model.ActivityFlyby
import ua.com.radiokot.flybys.strava.activities.ActivitiesService
import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.segments.LeaderboardsService
import ua.com.radiokot.flybys.strava.segments.model.LeaderboardResult
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.roundToInt

class FlybyAnalysis(
        private val activitiesService: ActivitiesService,
        private val leaderboardsService: LeaderboardsService,
) {
    fun getActivityFlybys(activity: Activity): List<ActivityFlyby> {
        Logger.getGlobal().log(Level.INFO, "Starting Flyby analysis of activity ${activity.id}...")

        val coverageEfforts = SegmentEffortsCoverageFinder.findMaxCoverage(activity.segmentEfforts)
        val coverageSegmentIds = coverageEfforts
                .map(SegmentEffort::segmentId)
                .toSet()

        Logger.getGlobal().log(Level.INFO, "Have to load ${coverageSegmentIds.size} leaderboards")

        val leaderboardResults = coverageSegmentIds
                .map(leaderboardsService::getTodaySegmentLeaderboard)
                .flatten()
        val activityToLoadIds = leaderboardResults
                .map(LeaderboardResult::activityId)
                .filterNot { it == activity.id }
                .toSet()

        Logger.getGlobal().log(Level.INFO, "Have to load ${activityToLoadIds.size} " +
                "that are possible Flybys")

        val activitiesToCheck = activityToLoadIds
                .map { activityId ->
                    activitiesService.getById(
                            activityId = activityId,
                            includeSegmentEfforts = false
                    )
                }
                .filter { loadedActivity ->
                    // Skip activities with time mismatch for now.
                    loadedActivity.endedAtLocal > activity.startedAtLocal
                            && loadedActivity.startedAtLocal < activity.endedAtLocal
                }

        Logger.getGlobal().log(Level.INFO, "Have to check ${activitiesToCheck.size} activities, " +
                " ${activityToLoadIds.size - activitiesToCheck.size} filtered out")

        val flybys = activitiesToCheck
                .mapNotNull { activityToCheck ->
                    val nearbyPoints = NearbyStreamPointsFinder
                            .findNearbyStreamPoints(activity, activityToCheck)

                    if (nearbyPoints.isNotEmpty()) {
                        ActivityFlyby(
                                activity = activityToCheck,
                                nearbyPointIndices = nearbyPoints,
                                correlationPercent = (nearbyPoints.size.toDouble() * 100 / activity.locationTimeStream.size)
                                        .roundToInt()
                        )
                    } else {
                        null
                    }
                }

        Logger.getGlobal().log(Level.INFO, "Found ${flybys.size} Flybys")

        return flybys
    }
}