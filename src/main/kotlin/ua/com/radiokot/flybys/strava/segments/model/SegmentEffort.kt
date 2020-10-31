package ua.com.radiokot.flybys.strava.segments.model

import com.fasterxml.jackson.databind.JsonNode

data class SegmentEffort(
        val id: String,
        val segmentId: String,
        val segmentName: String,
        val startIndex: Int,
        val endIndex: Int,
) {
    constructor(json: JsonNode): this(
            id = json["id"].asText(),
            segmentId = json["segment_id"].asText(),
            segmentName = json["name"].asText(),
            startIndex = json["start_index"].asInt(),
            endIndex = json["end_index"].asInt(),
    )

    val length: Int
        get() = endIndex - startIndex
}