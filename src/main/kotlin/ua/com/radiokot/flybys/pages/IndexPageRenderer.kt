package ua.com.radiokot.flybys.pages

import io.javalin.http.Context
import java.text.SimpleDateFormat
import java.util.*

class IndexPageRenderer : PageRenderer {
    override fun render(ctx: Context) {
        ctx.render("index.html", mapOf(
                "date" to SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Date())
        ))
    }
}