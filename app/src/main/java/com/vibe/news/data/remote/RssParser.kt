package com.vibe.news.data.remote

import android.util.Xml
import com.vibe.news.data.local.Article
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class RssParser {
    
    // Namespaces can be tricky in RSS, we ignore them mostly
    private val ns: String? = null

    fun parse(inputStream: InputStream, sourceName: String): List<Article> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser, sourceName)
        }
    }

    private fun readFeed(parser: XmlPullParser, sourceName: String): List<Article> {
        val entries = mutableListOf<Article>()
        
        // Search for <channel>
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            
            // Inside <rss>, usually there is <channel>
            if (parser.name == "channel") {
                entries.addAll(readChannel(parser, sourceName))
            } else {
                skip(parser)
            }
        }
        return entries
    }
    
    private fun readChannel(parser: XmlPullParser, sourceName: String): List<Article> {
        val entries = mutableListOf<Article>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "item") {
                entries.add(readItem(parser, sourceName))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    private fun readItem(parser: XmlPullParser, sourceName: String): Article {
        var title: String? = null
        var description: String? = null
        var link: String? = null
        var pubDate: String? = null
        var imageUrl: String? = null
        var category = "General"

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.name) {
                "title" -> title = readText(parser)
                "description" -> description = readText(parser)
                "link" -> link = readText(parser)
                "pubDate" -> pubDate = readText(parser)
                "category" -> category = readText(parser)
                "media:content", "enclosure" -> {
                    val url = parser.getAttributeValue(null, "url")
                    if (imageUrl == null && url != null) imageUrl = url
                    skip(parser) // Skip the tag content, we just wanted the attribute
                }
                else -> skip(parser)
            }
        }
        
        // Simple date parsing or current time fallback
        val dateLong = try {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(pubDate ?: "")?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return Article(
            title = title ?: "No Title",
            description = description,
            link = link ?: "",
            imageUrl = imageUrl, // This parser is basic, might miss some images
            source = sourceName,
            pubDate = dateLong,
            category = category
        )
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
