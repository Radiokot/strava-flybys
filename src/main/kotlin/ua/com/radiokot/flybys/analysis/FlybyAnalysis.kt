package ua.com.radiokot.flybys.analysis

import ua.com.radiokot.flybys.analysis.model.ActivityFlyby
import ua.com.radiokot.flybys.strava.activities.ActivitiesService
import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import java.util.logging.Level
import java.util.logging.Logger

class FlybyAnalysis(
        private val activitiesService: ActivitiesService,
) {
    fun getActivityFlybys(activity: Activity): List<ActivityFlyby> {
        Logger.getGlobal().log(Level.INFO, "Starting Flyby analysis of activity ${activity.id}...")

        val coverageEfforts = SegmentEffortsCoverageFinder.findMaxCoverage(activity.segmentEfforts)
        val coverageSegmentIds = coverageEfforts
                .map(SegmentEffort::segmentId)
                .toSet()

        Logger.getGlobal().log(Level.INFO, "Have to load ${coverageSegmentIds.size} leaderboards")

        return emptyList()
    }
}