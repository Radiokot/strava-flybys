package ua.com.radiokot.flybys.analysis

import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object NearbyStreamPointsFinder {
    private data class IndexedLocationTimePoint(
            val index: Int,
            val point: LocationTimePoint
    )

    /**
     * @return list of [targetActivity] points indices
     * that are nearby to points of [sourceActivity]
     */
    fun findNearbyStreamPoints(sourceActivity: Activity,
                               targetActivity: Activity): List<Int> {
        val possibleNearbyRangeStartTime = max(sourceActivity.startedAtLocal, targetActivity.startedAtLocal) -
                TIME_DELTA_THRESHOLD_S
        val possibleNearbyRangeEndTime = min(sourceActivity.endedAtLocal, targetActivity.endedAtLocal) -
                TIME_DELTA_THRESHOLD_S

        fun checkPointAbsoluteTime(point: LocationTimePoint, activity: Activity): Boolean {
            val pointAbsoluteTime = point.getAbsoluteTime(activity.startedAtLocal)
            return pointAbsoluteTime in possibleNearbyRangeStartTime..possibleNearbyRangeEndTime
        }

        val sourcePointsToCheck = sourceActivity.locationTimeStream
                .filter { checkPointAbsoluteTime(it, sourceActivity) }
        val targetPointsToCheck = targetActivity.locationTimeStream
                .mapIndexed(::IndexedLocationTimePoint)
                .filter { checkPointAbsoluteTime(it.point, targetActivity) }

        if (sourcePointsToCheck.isEmpty() || targetPointsToCheck.isEmpty()) {
            return emptyList()
        }

        val nearbyPoints = mutableSetOf<Int>()
        sourcePointsToCheck.forEach { sourcePoint ->
            targetPointsToCheck.forEach targetLoop@{ targetPoint ->
                val timeDelta = abs(sourcePoint.getAbsoluteTime(sourceActivity.startedAtLocal) -
                        targetPoint.point.getAbsoluteTime(targetActivity.startedAtLocal))

                if (timeDelta > TIME_DELTA_THRESHOLD_S) {
                    return@targetLoop
                }

                val distance = sourcePoint.getFastDistanceTo(targetPoint.point)

                if (distance < DISTANCE_DELTA_THRESHOLD_M) {
                    nearbyPoints.add(targetPoint.index)
                }
            }
        }

        return nearbyPoints.sorted()
    }

    private const val TIME_DELTA_THRESHOLD_S = 120
    private const val DISTANCE_DELTA_THRESHOLD_M = 300
}