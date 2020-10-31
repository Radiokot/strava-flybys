package ua.com.radiokot.flybys.analysis.model

import ua.com.radiokot.flybys.strava.activities.model.Activity

data class ActivityFlyby(
        val activity: Activity,
        val nearbyPointIndices: List<Int>,
        val correlationPercent: Double
)