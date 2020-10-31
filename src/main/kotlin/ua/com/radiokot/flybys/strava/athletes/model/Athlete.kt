package ua.com.radiokot.flybys.strava.athletes.model

import com.fasterxml.jackson.databind.JsonNode

data class Athlete(
        val id: String,
        val name: String,
        val avatarUrl: String?,
) {
    constructor(json: JsonNode): this(
            id = json["id"].asText(),
            name = json["display_name"].asText(),
            avatarUrl = json["photo"].asText(),
    )
}