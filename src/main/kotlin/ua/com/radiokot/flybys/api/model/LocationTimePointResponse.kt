package ua.com.radiokot.flybys.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint

class LocationTimePointResponse(
        @JsonProperty("lat")
        val lat: Double,
        @JsonProperty("lng")
        val lng: Double,
        @JsonProperty("time")
        val time: Int,
) {
    constructor(locationTimePoint: LocationTimePoint): this(
            lat = locationTimePoint.lat,
            lng = locationTimePoint.lng,
            time = locationTimePoint.timeOffsetSeconds
    )
}