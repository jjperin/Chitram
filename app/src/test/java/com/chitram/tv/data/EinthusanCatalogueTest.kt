package com.chitram.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class EinthusanCatalogueTest {
    private val catalogue = EinthusanCatalogue()

    @Test fun parsesResultListingsAndOneBasedPageCount() {
        val html = """
            <div class="results-info"><p>Page 1 of 57<span>( 341 results )</span></p></div>
            <section id="UIMovieSummary"><ul><li>
              <div class="block2"><a class="title" href="/movie/watch/6193/?lang=tamil"><h3>A Aa E Ee (Tamil)</h3><span class="popular">Must Watch</span></a>
              <div class="info"><p>2009<span>Tamil</span></p></div>
              <p class="synopsis">A village story.</p></div>
            </li></ul></section>
        """.trimIndent()
        val page = catalogue.parseResults(html, "Tamil")
        assertEquals(57, page.pageCount)
        assertEquals("6193", page.listings.single().sourceId)
        assertEquals("A Aa E Ee (Tamil)", page.listings.single().title)
        assertEquals(2009, page.listings.single().year)
        assertEquals("A village story.", page.listings.single().synopsis)
        assertEquals("https://einthusan.tv/movie/watch/6193/?lang=tamil", page.listings.single().pageUrl)
        assertEquals("https://einthusan.tv/movie/results/?alpha=A&find=Alphabets&lang=tamil&page=1",
            catalogue.alphabetUrl("Tamil", "A"))
    }

    @Test fun recentScanExcludesComingSoon() {
        val html = """
            <section id="UIFeaturedFilms"><div class="tabview"><a class="title" href="/movie/watch/AAA1/?lang=tamil">New Film</a><div class="info"><p>2025<span>Tamil</span></p></div></div></section>
            <section id="UIShowcasedFilms">
              <input id="_showcase_2"><div class="tabview"><ul><li><a class="title" href="/movie/watch/BBB2/?lang=tamil">Recently Added</a></li></ul></div>
              <input id="_showcase_4"><div class="tabview"><ul><li><a class="title" href="/movie/watch/CCC3/?lang=tamil">Coming Soon</a></li></ul></div>
            </section>
        """.trimIndent()
        val ids = catalogue.parseRecent(html, "Tamil").listings.map { it.sourceId }
        assertEquals(listOf("AAA1", "BBB2"), ids)
        assertFalse("CCC3" in ids)
    }

    @Test fun rejectsUnavailablePageWithoutClearingExistingData() {
        assertThrows(IllegalStateException::class.java) {
            catalogue.parseRecent("<h1>Service Unavailable.</h1>", "Tamil")
        }
    }

    @Test fun acceptsZeroResultsEvenWhenSiteReportsOnePage() {
        val page = catalogue.parseResults(
            "<div class='results-info'><p>Page 1 of 1<span>( 0 results )</span></p></div><section id='UIMovieSummary'><ul></ul></section>",
            "Tamil")
        assertEquals(1, page.pageCount)
        assertEquals(emptyList<SourceListing>(), page.listings)
    }
}
