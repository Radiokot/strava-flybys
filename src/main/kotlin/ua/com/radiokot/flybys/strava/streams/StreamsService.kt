package ua.com.radiokot.flybys.strava.streams

import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint

interface StreamsService {
    fun getActivityLocationTimeStream(activityId: String): List<LocationTimePoint>
}