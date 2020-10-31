package ua.com.radiokot.flybys.strava.streams.exceptions

class StreamNotFoundException(activityId: String):
        IllegalStateException("There is no stream for activity $activityId")