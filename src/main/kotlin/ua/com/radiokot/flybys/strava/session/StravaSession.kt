package ua.com.radiokot.flybys.strava.session

interface StravaSession {
    val email: String
    val stravaRootUrl: String
    fun getHeaders(): Map<String, String>
}