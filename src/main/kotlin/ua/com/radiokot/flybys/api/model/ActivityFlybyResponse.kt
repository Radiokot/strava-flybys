package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.flybys.analysis.model.ActivityFlyby

class ActivityFlybyResponse(
        @JsonProperty("activity")
        val activity: ActivityResponse,
        @JsonProperty("nearby_points")
        val nearbyPoints: List<Int>,
        @JsonProperty("correlation")
        val correlation: Int
) {
    constructor(activityFlyby: ActivityFlyby) : this(
            activity = ActivityResponse(activityFlyby.activity),
            nearbyPoints = activityFlyby.nearbyPointIndices,
            correlation = activityFlyby.correlationPercent
    )
}