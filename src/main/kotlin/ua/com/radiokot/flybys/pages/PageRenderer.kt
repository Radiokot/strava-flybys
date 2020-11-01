package ua.com.radiokot.flybys.pages

import io.javalin.http.Context

interface PageRenderer {
    fun render(ctx: Context)
}