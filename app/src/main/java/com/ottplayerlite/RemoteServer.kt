package com.ottplayerlite

import android.content.Context
import com.ottplayerlite.utils.NetworkManager
import fi.iki.elonen.NanoHTTPD

class RemoteServer(val context: Context, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val prefs = context.getSharedPreferences("ULTIMATE_PREFS", Context.MODE_PRIVATE)
        
        // Obsługa zapisu nowego URL przez POST
        if (session.method == Method.POST) {
            try {
                val files = HashMap<String, String>()
                session.parseBody(files)
                val newUrl = session.parameters["m3u_url"]?.get(0)
                if (newUrl != null) {
                    prefs.edit().putString("m3u_url", newUrl).apply()
                    return newFixedLengthResponse(Response.Status.OK, "text/html", "<h1>Zapisano! Zrestartuj aplikacje.</h1>")
                }
            } catch (e: Exception) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.message)
            }
        }

        // Prosty panel sterowania HTML
        val vpnStatus = if (NetworkManager.isVpnActive(context)) "AKTYWNY" else "NIEAKTYWNY"
        val currentUrl = prefs.getString("m3u_url", "brak")
        
        val html = """
            <html>
            <body style='background:#1a1a1a; color:gold; font-family:sans-serif; text-align:center;'>
                <h1>ULTIMATE PLAYER - Remote Control</h1>
                <p>Status VPN: <b>$vpnStatus</b></p>
                <p>Aktualna lista: <br><small>$currentUrl</small></p>
                <form method='POST'>
                    <input type='text' name='m3u_url' placeholder='Wklej link M3U' style='width:80%; padding:10px;'><br><br>
                    <input type='submit' value='ZAPISZ I ODŚWIEŻ' style='padding:10px 20px; background:gold;'>
                </form>
            </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(html)
    }
}
