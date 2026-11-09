package com.chitram.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbMetadataTest {
    private fun candidate(id: Long, title: String, year: Int, language: String = "ta") = TmdbMatch(id, title, title, language, year, "", "$year-01-01", 7.0, 1.0, null, null)

    @Test fun requiresExactTitleAndYear() {
        assertEquals(3L, chooseTmdbMatch("A Aa E Ee Tamil", 2009, "Tamil",
            listOf(candidate(3, "A Aa E Ee Tamil", 2009), candidate(4, "A Aa E Ee Tamil", 2011)))?.id)
        assertNull(chooseTmdbMatch("A Aa E Ee", 2009, "Tamil", listOf(candidate(3, "A Aa E Ee Tamil", 2009))))
    }

    @Test fun languageTagIsRemovedForMetadataLookupOnly() {
        assertEquals("A Aa E Ee", catalogueSearchTitle("A Aa E Ee (Tamil)"))
        assertEquals(3L, chooseTmdbMatch("A Aa E Ee (Tamil)", 2009, "Tamil",
            listOf(candidate(3, "A Aa E Ee", 2009)))?.id)
        assertEquals("An Unrelated (Film)", catalogueSearchTitle("An Unrelated (Film)"))
    }

    @Test fun rejectsAmbiguousMatches() {
        assertNull(chooseTmdbMatch("Example", 2020, "Tamil",
            listOf(candidate(1, "Example", 2020), candidate(2, "Example", 2020))))
        assertEquals(1L, chooseTmdbMatch("Example", 2020, "Tamil",
            listOf(candidate(1, "Example", 2020, "ta"), candidate(2, "Example", 2020, "ml")))?.id)
    }
}
