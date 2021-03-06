import org.junit.Assert
import org.junit.Test
import ua.com.radiokot.flybys.strava.http.RequestRateLimiter
import kotlin.math.roundToInt

class RequestRateLimiterTest {
    @Test
    fun singleRequest() {
        RequestRateLimiter.reset()

        val startTime = System.currentTimeMillis()
        RequestRateLimiter.waitBeforeRequest()
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
        repeat(count) { RequestRateLimiter.waitBeforeRequest() }

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