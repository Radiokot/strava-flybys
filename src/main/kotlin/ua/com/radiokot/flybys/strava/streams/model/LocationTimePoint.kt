package ua.com.radiokot.flybys.strava.streams.model

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
}