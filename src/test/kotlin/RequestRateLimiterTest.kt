import org.junit.Assert
import org.junit.Test
import ua.com.radiokot.flybys.strava.RequestRateLimiter
import kotlin.math.roundToInt

class RequestRateLimiterTest {
    @Test
    fun singleRequest() {
        RequestRateLimiter.reset()

        val startTime = System.currentTimeMillis()
        RequestRateLimiter.awaitForRequest()
        val elapsed = (System.currentTimeMillis() - startTime)

        Assert.assertTrue(
                "There must be no significant delay for a single first request",
                elapsed <= 50
        )
    }

    @Test
    fun requestsInSeries() {
        RequestRateLimiter.reset()

        val startTime = System.currentTimeMillis()

        val count = 5
        repeat(count) { RequestRateLimiter.awaitForRequest() }

        val elapsed = (System.currentTimeMillis() - startTime).toDouble()

        val expectedDelays = count - 1
        Assert.assertEquals(
                "There must be $expectedDelays delays ${RequestRateLimiter.REQUEST_TIMEOUT_MS} ms each",
                expectedDelays,
                (elapsed / RequestRateLimiter.REQUEST_TIMEOUT_MS).roundToInt(),
        )
    }

    @Test
    fun requestAfterLongTime() {
        RequestRateLimiter.reset()


    }
}