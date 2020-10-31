package ua.com.radiokot.flybys.strava.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

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
                        val logger = HttpLoggingInterceptor.Logger {message ->
                            println("HttpClient: $message")
                        }

                        addInterceptor(HttpLoggingInterceptor(logger).apply {
                            setLevel(HttpLoggingInterceptor.Level.BASIC)
                        })
                    }
                }
                .build()
    }
}