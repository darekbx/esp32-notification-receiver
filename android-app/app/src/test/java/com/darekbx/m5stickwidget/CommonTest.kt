package com.darekbx.m5stickwidget

import org.junit.Test

import org.junit.Assert.*

class CommonTest {

    @Test
    fun `Arduino make 3 chunks`() {
        val input = "Calendar,(No title),15:18 16:10"

        val triple = makeChunks(input)
        val application = triple.first
        var title = triple.second
        var subTitle = triple.third

        assertEquals("Calendar", application)
        assertEquals("(No title)", title)
        assertEquals("15:18 16:10", subTitle)
    }

    @Test
    fun `Arduino make 2 chunks`() {
        val input = "Calendar,(No title)"

        val triple = makeChunks(input)
        val application = triple.first
        var title = triple.second
        var subTitle = triple.third

        assertEquals("Calendar", application)
        assertEquals("(No title)", title)
    }

    private fun makeChunks(input: String): Triple<String, String, String> {
        val first = input.indexOf(',')
        val application = input.substring(0, first)
        val second = input.indexOf(',', first + 1)

        var title = ""
        var subTitle = ""

        if (second == -1) {
            title = input.substring(first + 1)
        } else {
            title = input.substring(first + 1, second)
            subTitle = input.substring(second + 1)
        }

        return Triple(application, title, subTitle)
    }
}