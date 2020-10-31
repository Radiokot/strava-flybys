package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty

class ScheduleFlybyTaskResponseBody(
        @JsonProperty("task_id")
        val taskId: String
)