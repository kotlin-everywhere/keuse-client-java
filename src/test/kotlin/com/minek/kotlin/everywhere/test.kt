package com.minek.kotlin.everywhere

import com.minek.kotlin.everywhere.kelibs.result.ok
import com.minek.kotlin.everywhere.keuse.testCommon.TestCrate
import org.junit.Assert.assertEquals
import org.junit.Test

val testCrate = TestCrate().apply { i(remote = "http://localhost:8000") }

class TestKeuse {
    @Test
    fun testSimple() {
        assertEquals(ok(3), testCrate.add(TestCrate.AddReq(1, 2)))
    }

    @Test
    fun testNested() {
        assertEquals(ok(true), testCrate.inner.flip(false))
    }
}