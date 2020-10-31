package ua.com.radiokot.flybys.strava.http

import okhttp3.Headers
import okhttp3.Request

fun Request.Builder.addHeaders(headers: Headers) = apply {
    headers.forEach { addHeader(it.first, it.second) }
}