package ua.com.radiokot.flybys.strava.activities

import ua.com.radiokot.flybys.strava.activities.model.Activity

interface ActivitiesService {
    fun getById(activityId: String,
                includeSegmentEfforts: Boolean = true): Activity
}