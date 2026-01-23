package com.ottplayerlite

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream

data class EpgProgram(
    val title: String,
    val description: String,
    val start: Long,
    val stop: Long
)

object EpgParser {
    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    fun parseGz(inputStream: InputStream, targetChannelId: String): List<EpgProgram> {
        val programs = mutableListOf<EpgProgram>()
        val gzipStream = GZIPInputStream(inputStream)
        val parser = Xml.newPullParser()
        parser.setInput(gzipStream, "UTF-8")

        var eventType = parser.eventType
        var currentChannel: String? = null
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "programme") {
                        currentChannel = parser.getAttributeValue(null, "channel")
                        if (currentChannel == targetChannelId) {
                            val start = dateFormat.parse(parser.getAttributeValue(null, "start"))?.time ?: 0
                            val stop = dateFormat.parse(parser.getAttributeValue(null, "stop"))?.time ?: 0
                            
                            var title = ""; var desc = ""
                            while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "programme")) {
                                if (parser.eventType == XmlPullParser.START_TAG) {
                                    if (parser.name == "title") title = parser.nextText()
                                    if (parser.name == "desc") desc = parser.nextText()
                                }
                            }
                            programs.add(EpgProgram(title, desc, start, stop))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return programs
    }
}
