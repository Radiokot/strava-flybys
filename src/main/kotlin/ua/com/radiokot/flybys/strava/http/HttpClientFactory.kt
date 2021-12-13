package ua.com.radiokot.flybys.strava.http

import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Duration

/**
 * Creates [OkHttpClient] to work with Strava API.
 *
 * @param stravaRootUrl i.e. https://www.strava.com
 * @param stravaHostname i.e. www.strava.com
 */
class HttpClientFactory(
    private val stravaRootUrl: String,
    private val stravaHostname: String,
    private val withLogs: Boolean = true,
) {
    val httpClient: OkHttpClient by lazy {
        val mainHeaders = FakeHeaders.getMainHeaders(stravaRootUrl, stravaHostname)

        OkHttpClient.Builder()
            .followRedirects(false)
            .callTimeout(Duration.ofSeconds(30))
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .apply {
                        mainHeaders.forEach { (key, value) ->
                            addHeader(key, value)
                        }
                    }
                    .build())
            }
            .apply {
                if (withLogs) {
                    val kLogger = KotlinLogging.logger("HTTP")
                    val logger = HttpLoggingInterceptor.Logger(kLogger::info)

                    addInterceptor(HttpLoggingInterceptor(logger).apply {
                        setLevel(HttpLoggingInterceptor.Level.BASIC)
                    })
                }
            }
            .build()
    }
}