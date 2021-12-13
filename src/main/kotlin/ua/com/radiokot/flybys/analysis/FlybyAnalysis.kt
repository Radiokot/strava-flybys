package ua.com.radiokot.flybys.analysis

import mu.KotlinLogging
import ua.com.radiokot.flybys.analysis.model.ActivityFlyby
import ua.com.radiokot.flybys.strava.activities.ActivitiesService
import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.segments.LeaderboardsService
import ua.com.radiokot.flybys.strava.segments.model.LeaderboardResult
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import kotlin.math.roundToInt

class FlybyAnalysis(
    private val activitiesService: ActivitiesService,
    private val leaderboardsService: LeaderboardsService,
) {
    private val logger = KotlinLogging.logger("FlybyAnalysis")

    fun getActivityFlybys(activity: Activity): List<ActivityFlyby> {
        logger.info {
            "Starting Flyby analysis of activity ${activity.id}..."
        }

        val coverageEfforts = SegmentEffortsCoverageFinder.findMaxCoverage(activity.segmentEfforts)
        val coverageSegmentIds = coverageEfforts
            .map(SegmentEffort::segmentId)
            .toSet()

        logger.debug { "Have to load ${coverageSegmentIds.size} leaderboards" }

        val leaderboardResults = coverageSegmentIds
            .map(leaderboardsService::getTodaySegmentLeaderboard)
            .flatten()
        val activityToLoadIds = leaderboardResults
            .map(LeaderboardResult::activityId)
            .filterNot { it == activity.id }
            .toSet()

        logger.debug {
            "Have to load ${activityToLoadIds.size} activities" +
                    "that are possible Flybys"
        }

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

        logger.debug {
            "Have to check ${activitiesToCheck.size} activities, " +
                    "${activityToLoadIds.size - activitiesToCheck.size} filtered out"
        }

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
                            .coerceAtLeast(1)
                    )
                } else {
                    null
                }
            }

        logger.info {
            "Found ${flybys.size} Flybys"
        }

        return flybys
    }
}