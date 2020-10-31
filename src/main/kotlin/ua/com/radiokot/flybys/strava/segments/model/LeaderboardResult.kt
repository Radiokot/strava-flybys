package ua.com.radiokot.flybys.strava.segments.model

import com.fasterxml.jackson.databind.JsonNode

data class LeaderboardResult(
        val id: String,
        val activityId: String,
) {
    constructor(json: JsonNode): this(
            id = json["id"].asText(),
            activityId = json["activity_id"].asText(),
    )
}