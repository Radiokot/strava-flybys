package ua.com.radiokot.flybys.strava.http

object FakeHeaders {
    val extraForJsonResponse = mapOf(
            "x-requested-with" to "XMLHttpRequest",
            "accept" to "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"
    )

    val extraForHtmlResponse = mapOf(
            "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
    )

    fun getMainHeaders(stravaRootUrl: String,
                       stravaHostname: String) = mapOf(
            "authority" to stravaHostname,
            "origin" to stravaRootUrl,
            "upgrade-insecure-requests" to "1",
            "dnt" to "1",
            "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36",
            "sec-fetch-site" to "same-origin",
            "sec-fetch-mode" to "navigate",
            "sec-fetch-dest" to "empty",
            "accept-language" to "en-US,en;q=0.9"
    )
}