package ua.com.radiokot.flybys.strava.streams.model

import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LocationTimePoint(
        val lat: Double,
        val lng: Double,

        /**
         * Time offset from activity start in seconds
         */
        val timeOffsetSeconds: Int
) {
    fun getAbsoluteTime(activityStartedAt: Long) =
            activityStartedAt + timeOffsetSeconds

    /**
     * @return distance from this point to [to] in meters
     */
    fun getFastDistanceTo(to: LocationTimePoint): Int {
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val toLatRad = Math.toRadians(to.lat)
        val toLngRad = Math.toRadians(to.lng)

        val x = (toLngRad - lngRad) * cos(0.5 * (toLatRad + latRad))
        val y = toLatRad - latRad

        return (EARTH_RADIUS_M * sqrt(x * x + y * y)).roundToInt()
    }

    private companion object {
        private const val EARTH_RADIUS_M = 6371000
    }
}