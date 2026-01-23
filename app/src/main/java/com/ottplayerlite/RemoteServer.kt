package com.ottplayerlite

import fi.iki.elonen.NanoHTTPD
import android.content.Context

class RemoteServer(val context: Context, port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val html = """
            <html>
                <body style='background: #121212; color: #FFBB00; font-family: sans-serif; text-align: center;'>
                    <h1>Ultimate Player - Remote Control</h1>
                    <p>Serwer aktywny. Zarządzaj swoją listą kanałów.</p>
                </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }
}
