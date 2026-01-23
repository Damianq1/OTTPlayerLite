package com.ottplayerlite

import fi.iki.elonen.NanoHTTPD

class RemoteServer(port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val msg = """<html><body><h1>Ultimate Player Remote</h1><p>Serwer aktywny.</p></body></html>"""
        return newFixedLengthResponse(msg)
    }
}
