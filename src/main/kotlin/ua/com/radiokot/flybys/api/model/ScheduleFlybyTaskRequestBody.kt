package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class ScheduleFlybyTaskRequestBody(
        @JsonProperty("activity_id")
        val activityId: String
)