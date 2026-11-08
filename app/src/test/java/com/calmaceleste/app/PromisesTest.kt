package com.calmaceleste.app

import com.calmaceleste.app.data.Promises
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromisesTest {
    @Test
    fun hasPromisesInEveryCategory() {
        Promises.categories.indices.forEach { c ->
            val n = Promises.all.count { it.category == c }
            assertTrue("La categoría ${Promises.categories[c]} tiene solo $n", n >= 5)
        }
    }

    @Test
    fun referencesAreUniqueAndTextsNotEmpty() {
        assertEquals(Promises.all.size, Promises.all.map { it.ref }.toSet().size)
        Promises.all.forEach { assertTrue(it.text.length > 10 && it.ref.isNotBlank()) }
    }

    @Test
    fun todayIsInList() {
        assertTrue(Promises.today() in Promises.all)
    }
}
