package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.flybys.strava.activities.model.Activity

class ActivityResponse(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("athlete")
        val athlete: AthleteResponse,
        @JsonProperty("stream")
        val stream: List<LocationTimePointResponse>
) {
    constructor(activity: Activity): this(
            id = activity.id,
            name = activity.name,
            athlete = AthleteResponse(activity.athlete),
            stream = activity.locationTimeStream.map(::LocationTimePointResponse)
    )
}