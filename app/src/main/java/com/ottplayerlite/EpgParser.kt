package com.ottplayerlite

import java.net.URL
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class EpgParser {
    fun parseEpg(url: String): Map<String, String> {
        val epgData = mutableMapOf<String, String>()
        try {
            val stream = URL(url).openStream()
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            var currentChannelId = ""
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "programme") {
                    currentChannelId = parser.getAttributeValue(null, "channel")
                } else if (eventType == XmlPullParser.START_TAG && parser.name == "title" && currentChannelId.isNotEmpty()) {
                    epgData[currentChannelId] = parser.nextText()
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return epgData
    }
}
