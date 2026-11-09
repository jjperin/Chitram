package com.chitram.tv.data

import java.net.HttpURLConnection
import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class SourceListing(
    val sourceId: String,
    val title: String,
    val language: String,
    val year: Int,
    val synopsis: String,
    val pageUrl: String
)

data class SourcePage(val listings: List<SourceListing>, val pageCount: Int)

/** Reads only visible catalogue metadata. Video URLs and player requests are never inspected. */
class EinthusanCatalogue {
    private val languages = DemoCatalogue.languages
    private val idPattern = Regex("^/movie/watch/([A-Za-z0-9]+)/")
    private val pagePattern = Regex("Page\\s+\\d+\\s+of\\s+(\\d+)")
    private val resultCountPattern = Regex("\\(\\s*(\\d+)\\s+results\\s*\\)")

    fun recentUrl(language: String): String = "https://einthusan.tv/movie/browse/?lang=${slug(language)}"

    fun alphabetUrl(language: String, letter: String, page: Int = 1): String {
        require(letter == "Numbers" || letter.length == 1 && letter[0] in 'A'..'Z')
        require(page >= 1)
        val filter = if (letter == "Numbers") "find=Numbers" else "alpha=$letter&find=Alphabets"
        return "https://einthusan.tv/movie/results/?$filter&lang=${slug(language)}&page=$page"
    }

    fun fetch(url: String): String {
        val parsed = URL(url)
        require(parsed.protocol == "https" && parsed.host == "einthusan.tv")
        val connection = parsed.openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "text/html")
        return try {
            if (connection.responseCode != 200) error("Catalogue HTTP ${connection.responseCode}")
            if (connection.url.host != "einthusan.tv") error("Catalogue redirected away from Einthusan")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                .also { if (it.length > 1_500_000) error("Catalogue page exceeds size limit") }
        } finally {
            connection.disconnect()
        }
    }

    fun parseRecent(html: String, language: String): SourcePage {
        val document = Jsoup.parse(html)
        rejectUnavailable(document.text())
        val cards = document.select("#UIFeaturedFilms .tabview, #_showcase_2 + .tabview > ul > li")
        val listings = cards.mapNotNull { parseCard(it, language) }.distinctBy { it.sourceId }
        if (listings.isEmpty()) error("No recognizable recent listings for $language")
        return SourcePage(listings, 1)
    }

    fun parseResults(html: String, language: String): SourcePage {
        val document = Jsoup.parse(html)
        rejectUnavailable(document.text())
        val info = document.selectFirst(".results-info p")?.text() ?: error("Missing catalogue page count")
        val count = pagePattern.find(info)?.groupValues?.get(1)?.toIntOrNull()
            ?: error("Unrecognized catalogue page count")
        val resultCount = resultCountPattern.find(info)?.groupValues?.get(1)?.toIntOrNull()
            ?: error("Unrecognized catalogue result count")
        if (count !in 0..500) error("Unexpected catalogue page count: $count")
        val listings = document.select("#UIMovieSummary > ul > li").mapNotNull { parseCard(it, language) }
        if (resultCount > 0 && listings.isEmpty()) error("No recognizable movie listings on a nonempty page")
        return SourcePage(listings.distinctBy { it.sourceId }, count)
    }

    private fun parseCard(card: Element, language: String): SourceListing? {
        val link = card.selectFirst("a.title[href*=/movie/watch/]") ?: return null
        val path = link.attr("href")
        val id = idPattern.find(path)?.groupValues?.get(1) ?: return null
        val title = (link.selectFirst("h2, h3")?.text() ?: link.ownText()).trim()
        if (title.isBlank()) return null
        val year = Regex("\\b(19|20)\\d{2}\\b").find(card.selectFirst(".info p")?.ownText().orEmpty())
            ?.value?.toIntOrNull() ?: 0
        return SourceListing(id, title, language, year,
            card.selectFirst(".synopsis")?.text()?.trim().orEmpty(),
            "https://einthusan.tv/movie/watch/$id/?lang=${slug(language)}")
    }

    private fun rejectUnavailable(text: String) {
        if (text.contains("Service Unavailable.") || text.contains("Access Denied") || text.contains("Please enable cookies")) {
            error("Einthusan is unavailable from this network; existing catalogue was kept")
        }
    }

    private fun slug(language: String): String {
        require(language in languages) { "Unsupported catalogue language: $language" }
        return language.lowercase()
    }
}
