package org.ccgemp.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SplitUtilsTest {
    @Test
    fun testSplitAndMerge() {
        val testTexts = listOf("aa", "b,b", "cc")
        val result = testTexts.mergeTexts(',').splitText(',')
        assertEquals(testTexts, result)
    }

    @Test
    fun testEmpty() {
        val testTexts = listOf("", "b", "")
        val result = testTexts.mergeTexts(',').splitText(',')
        assertEquals(testTexts, result)
    }

    @Test
    fun testOneEmptyElement() {
        val testTexts = listOf("")
        val result = testTexts.mergeTexts(',').splitText(',')
        assertEquals(testTexts, result)
    }

    @Test
    fun testEmptyList() {
        val testTexts = listOf<String>()
        val result = testTexts.mergeTexts(',').splitText(',')
        assertEquals(testTexts, result)
    }

    @Test
    fun testMax() {
        val testTexts = listOf("a", "b", "c")
        val result = testTexts.mergeTexts(',').splitText(',', 2)
        assertEquals(listOf("a", "b,c"), result)
    }

    @Test
    fun testMaxAndEscape() {
        val testTexts = listOf("a", "b,b", "c")
        val result = testTexts.mergeTexts(',').splitText(',', 2)
        assertEquals(listOf("a", "b,b,c"), result)
    }
}
