package ua.com.radiokot.flybys.strava.segments

import ua.com.radiokot.flybys.strava.segments.model.LeaderboardResult

interface LeaderboardsService {
    fun getTodaySegmentLeaderboard(segmentId: String): List<LeaderboardResult>
}