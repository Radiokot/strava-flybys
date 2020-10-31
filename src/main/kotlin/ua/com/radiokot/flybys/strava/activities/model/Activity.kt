package ua.com.radiokot.flybys.strava.activities.model

import ua.com.radiokot.flybys.strava.athletes.model.Athlete
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint
import java.util.*

data class Activity(
        val id: String,
        val name: String,
        val athlete: Athlete,
        val startedAtLocal: Long,
        val endedAtLocal: Long,
        val locationTimeStream: List<LocationTimePoint>,
        val segmentEfforts: List<SegmentEffort>
)