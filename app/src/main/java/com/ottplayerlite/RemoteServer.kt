package com.ottplayerlite

import fi.iki.elonen.NanoHTTPD
import android.content.Context

class RemoteServer(val context: Context, port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val params = session.parameters
        if (params.containsKey("url")) {
            val newUrl = params["url"]?.get(0) ?: ""
            context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                .edit().putString("last_url", newUrl).apply()
            return newFixedLengthResponse("<html><body><h1>URL zapisany! zrestartuj apke.</h1></body></html>")
        }
        
        val html = """
            <html>
            <body style='background:#121212; color:white; font-family:sans-serif; text-align:center;'>
                <h1>OttPlayer PRO - Zdalne dodawanie listy</h1>
                <form action='/' method='get'>
                    <input type='text' name='url' style='width:80%; padding:10px;' placeholder='Wklej link M3U8 tutaj'><br><br>
                    <input type='submit' value='WYŚLIJ DO TV' style='padding:10px 20px; background:#FFBB00; border:none;'>
                </form>
            </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(html)
    }
}
