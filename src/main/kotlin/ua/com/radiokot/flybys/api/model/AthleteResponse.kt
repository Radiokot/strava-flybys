package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.flybys.strava.athletes.model.Athlete

class AthleteResponse(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("avatar_url")
        val avatarUrl: String?,
) {
    constructor(athlete: Athlete): this(
            id = athlete.id,
            name = athlete.name,
            avatarUrl = athlete.avatarUrl
    )
}