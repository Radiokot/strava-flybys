import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test
import ua.com.radiokot.flybys.analysis.SegmentEffortsCoverageFinder
import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import java.io.File


class SegmentEffortsCoverageFinderTest {
    @Test
    fun maxCoverageSimple() {
        val efforts = listOf(
                SegmentEffort(
                        startIndex = 0,
                        endIndex = 3,
                        segmentName = "A",
                        id = "A",
                        segmentId = "A",
                ),
                SegmentEffort(
                        startIndex = 2,
                        endIndex = 8,
                        segmentName = "B",
                        id = "B",
                        segmentId = "B",
                ),
                SegmentEffort(
                        startIndex = 10,
                        endIndex = 14,
                        segmentName = "C",
                        id = "C",
                        segmentId = "C",
                ),
                SegmentEffort(
                        startIndex = 14,
                        endIndex = 15,
                        segmentName = "D",
                        id = "D",
                        segmentId = "D",
                )
        )

        val maxCoverage = SegmentEffortsCoverageFinder.findMaxCoverage(efforts)

        Assert.assertEquals(
                "B, C, D",
                maxCoverage.map(SegmentEffort::segmentName).joinToString()
        )
        assertNoOverlaps(maxCoverage)
    }

    @Test
    fun maxCoverageReal() {
        val url = this.javaClass.getResource("/3779833291_segment_efforts.json")
        val efforts = ObjectMapper().readTree(File(url.file)).map(::SegmentEffort)

        val maxCoverage = SegmentEffortsCoverageFinder.findMaxCoverage(efforts)

        Assert.assertEquals(
                "окружная Ст.Салтов, Saltiv dam, Saltov Uphill, Молодовая-Окружная, СалтШ-МоскПр, kharkov-chuguev, Чугуев - Печенеги , Pechenegy - Martovaya, Martovaya - Pechenegy, Kicevka up, Кочеток - Чугуев, Чугуев Uphill на Харьков, Чугуев-Харьков",
                maxCoverage.map(SegmentEffort::segmentName).joinToString()
        )
        assertNoOverlaps(maxCoverage)
    }

    private fun assertNoOverlaps(coverage: List<SegmentEffort>) {
        coverage.forEachIndexed { i, segment ->
            if (i > 0 && segment.startIndex < coverage[i - 1].endIndex) {
                println("Here:")
                println(segment.segmentName)
                throw AssertionError("Coverage contains overlaps")
            }
        }
    }
}