import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test
import ua.com.radiokot.flybys.analysis.NearbyStreamPointsFinder
import ua.com.radiokot.flybys.strava.activities.model.Activity
import ua.com.radiokot.flybys.strava.athletes.model.Athlete
import ua.com.radiokot.flybys.strava.streams.model.LocationTimePoint
import java.io.File

class NearbyStreamPointsFinderTest {
    @Test
    fun nearbyStreamPointsFinder() {
        val sourceActivity = Activity(
                id = "1",
                name = "Source",
                athlete = Athlete(
                        id = "1",
                        name = "Oleg",
                        avatarUrl = null
                ),
                startedAtLocal = 1598690794,
                endedAtLocal = 8564 + 1598690794,
                segmentEfforts = emptyList(),
                locationTimeStream = loadStream("stream_a.json")
        )

        val targetActivity = Activity(
                id = "2",
                name = "Target",
                athlete = Athlete(
                        id = "1",
                        name = "Pasha",
                        avatarUrl = null
                ),
                startedAtLocal = 1598691345,
                endedAtLocal = 14087 + 1598691345,
                segmentEfforts = emptyList(),
                locationTimeStream = loadStream("stream_b.json")
        )

        val nearbyPoints = NearbyStreamPointsFinder.findNearbyStreamPoints(sourceActivity, targetActivity)

        Assert.assertEquals(5788, nearbyPoints.size)

        nearbyPoints.forEach { index ->
            Assert.assertTrue(
                    "Point with index $index must be a point of target activity",
                    index < targetActivity.locationTimeStream.size
            )
        }
    }

    @Test
    fun noPoints() {
        val sourceActivity = Activity(
                id = "1",
                name = "Source",
                athlete = Athlete(
                        id = "1",
                        name = "Oleg",
                        avatarUrl = null
                ),
                startedAtLocal = 1598690794,
                endedAtLocal = 8564 + 1598690794,
                segmentEfforts = emptyList(),
                locationTimeStream = emptyList()
        )

        val targetActivity = Activity(
                id = "2",
                name = "Target",
                athlete = Athlete(
                        id = "1",
                        name = "Pasha",
                        avatarUrl = null
                ),
                startedAtLocal = 1598691345,
                endedAtLocal = 14087 + 1598691345,
                segmentEfforts = emptyList(),
                locationTimeStream = emptyList()
        )

        val nearbyPoints = NearbyStreamPointsFinder.findNearbyStreamPoints(sourceActivity, targetActivity)

        Assert.assertEquals(0, nearbyPoints.size)
    }

    private fun loadStream(fileName: String): List<LocationTimePoint> {

        val url = this.javaClass.getResource("/$fileName")
        val rawJson = ObjectMapper().readTree(File(url.file))
        val latLngs = rawJson["latlng"]
        val timestamps = rawJson["time"]

        return latLngs.mapIndexed { i, latLng ->
            LocationTimePoint(
                    lat = latLng[0].asDouble(),
                    lng = latLng[1].asDouble(),
                    timeOffsetSeconds = timestamps[i].asInt()
            )
        }
    }
}