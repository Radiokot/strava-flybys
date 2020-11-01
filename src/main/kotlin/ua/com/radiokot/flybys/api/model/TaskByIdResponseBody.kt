package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty

class TaskByIdResponseBody(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("state")
        val state: TaskStateResponse,
)