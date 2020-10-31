package ua.com.radiokot.flybys.analysis

import ua.com.radiokot.flybys.strava.segments.model.SegmentEffort
import kotlin.math.max

/**
 * Finds a subset of activity segment efforts that cover
 * the most of activity path without overlapping each other.
 */
object SegmentEffortsCoverageFinder {
    fun findMaxCoverage(efforts: List<SegmentEffort>): List<SegmentEffort> {
        val coverage = mutableListOf<SegmentEffort>()

        var currentRangeEnd = 0
        val currentRange = mutableListOf<SegmentEffort>()

        fun addCoverageForCurrentRange() {
            if (currentRange.size == 1) {
                coverage.add(currentRange.first())
            } else {
                coverage.addAll(findCoverageDynamic(currentRange))
            }
        }

        efforts.forEachIndexed { i, effort ->
            if (effort.startIndex >= currentRangeEnd && currentRange.isNotEmpty()) {
                addCoverageForCurrentRange()
                currentRangeEnd = 0
                currentRange.clear()
            }

            currentRange.add(effort)
            currentRangeEnd = max(effort.endIndex, currentRangeEnd)

            if (i == efforts.size - 1) {
                addCoverageForCurrentRange()
            }
        }

        return coverage
    }

    private fun findCoverageDynamic(efforts: List<SegmentEffort>): List<SegmentEffort> {
        require(efforts.isNotEmpty()) { "There is no segments" }

        val rangeStart = efforts.first().startIndex

        val count = efforts.size
        val maxEnd = efforts.maxByOrNull(SegmentEffort::endIndex)!!.endIndex - rangeStart

        // f(iEffort, jLength) = max(with this effort ; without this effort)
        // Each cell contains best coverage for given efforts count (i) and max length (j)
        // See 'help' dir.
        val field = Array(count + 1) {
            Array(maxEnd + 1) { 0 }
        }

        for (i in (1..count)) {
            for (j in (1..maxEnd)) {
                val effort = efforts[i - 1]

                val withoutIt = field[i - 1][j]

                if (j >= effort.endIndex - rangeStart) {
                    val beforeWithIt = field[i - 1][effort.startIndex - rangeStart]
                    val withIt = beforeWithIt + effort.length

                    field[i][j] =
                            if (withIt > withoutIt)
                                withIt
                            else
                                withoutIt
                } else {
                    field[i][j] = withoutIt
                }
            }
        }

        val res = mutableListOf<SegmentEffort>()
        var j = maxEnd
        for (i in (count downTo 1)) {
            val current = field[i][j]
            val withoutIt = field[i - 1][j]

            if (current != withoutIt) {
                val effort = efforts[i - 1]
                res.add(effort)
                j = effort.startIndex - rangeStart
            }
        }

        return res.asReversed()
    }
}