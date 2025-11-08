package com.gf.mail.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleTest {
    @Test
    fun simpleTestPasses() {
        assertTrue(true)
    }
    
    @Test
    fun anotherSimpleTest() {
        val result = 2 + 2
        assertTrue(result == 4)
    }
}