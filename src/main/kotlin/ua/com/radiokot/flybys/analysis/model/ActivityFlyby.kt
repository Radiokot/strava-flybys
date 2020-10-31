package ua.com.radiokot.flybys.analysis.model

import ua.com.radiokot.flybys.strava.activities.model.Activity

data class ActivityFlyby(
        /**
         * Activity that is Flyby to the source one
         */
        val activity: Activity,

        /**
         * Indices of [activity] points that are nearby to source activity
         */
        val nearbyPointIndices: List<Int>,

        /**
         * Percent of source and [activity] location-time correlation
         */
        val correlationPercent: Int
)