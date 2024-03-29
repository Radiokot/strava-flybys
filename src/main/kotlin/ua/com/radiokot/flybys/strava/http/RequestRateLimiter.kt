package ua.com.radiokot.flybys.strava.http

import org.jetbrains.annotations.TestOnly
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Limits Strava requests rate by providing timeouts for requestors.
 */
object RequestRateLimiter {
    const val REQUEST_TIMEOUT_MS = 1400L

    private val queue = mutableListOf<CountDownLatch>()
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            name = "RequestRateLimiter"
            isDaemon = true
        }
    }

    /**
     * Suspends current thread for a time required to safely
     * perform Strava request
     */
    fun waitBeforeRequest() = synchronized(queue) {
        val currentLatch = queue.lastOrNull() ?: CountDownLatch(0)

        val latchForNext = CountDownLatch(1)
        queue.add(latchForNext)

        val waitFor = REQUEST_TIMEOUT_MS * queue.size

        executor.schedule({
            queue.remove(latchForNext)
            latchForNext.countDown()
        }, waitFor, TimeUnit.MILLISECONDS)

        currentLatch.await()
    }

    @TestOnly
    fun reset() {
        queue.clear()
    }
}