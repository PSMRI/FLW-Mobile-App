package org.piramalswasthya.sakhi.database.room.migrations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationsTest {

    private fun pairs() = ALL_MIGRATIONS.map { it.startVersion to it.endVersion }

    @Test
    fun `registry lists all 64 migrations`() {
        assertEquals(64, ALL_MIGRATIONS.size)
    }

    @Test
    fun `registry has no duplicate version pairs`() {
        assertEquals(pairs().size, pairs().toSet().size)
    }

    @Test
    fun `every migration moves exactly forward`() {
        ALL_MIGRATIONS.forEach {
            assertTrue("${it.startVersion}->${it.endVersion}", it.endVersion > it.startVersion)
        }
    }

    @Test
    fun `oldest supported version is 13 and newest is 77`() {
        assertEquals(13, ALL_MIGRATIONS.minOf { it.startVersion })
        assertEquals(77, ALL_MIGRATIONS.maxOf { it.endVersion })
    }

    @Test
    fun `both upgrade routes into version 18 are kept`() {
        assertTrue(pairs().contains(16 to 18))
        assertTrue(pairs().contains(17 to 18))
    }

    @Test
    fun `every version from 13 to 76 has a path up to 77`() {
        val byStart = ALL_MIGRATIONS.groupBy { it.startVersion }
        for (start in 13..76) {
            var version = start
            while (version < 77) {
                val next = byStart[version]?.maxOf { it.endVersion }
                assertNotNull("no migration from $version", next)
                version = next!!
            }
            assertEquals(77, version)
        }
    }

    @Test
    fun `current production version 64 upgrades through 65 to 77 one step at a time`() {
        val path = mutableListOf(64)
        var version = 64
        while (version < 77) {
            version = ALL_MIGRATIONS.single { it.startVersion == version }.endVersion
            path.add(version)
        }
        assertEquals(listOf(64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77), path)
    }

    @Test
    fun `registry entries are the migration objects`() {
        assertTrue(ALL_MIGRATIONS.contains(Migration13To14))
        assertTrue(ALL_MIGRATIONS.contains(Migration64To65))
        assertTrue(ALL_MIGRATIONS.contains(Migration69To70))
        assertTrue(ALL_MIGRATIONS.contains(Migration70To71))
        assertTrue(ALL_MIGRATIONS.contains(Migration71To72))
        assertTrue(ALL_MIGRATIONS.contains(Migration72To73))
        assertTrue(ALL_MIGRATIONS.contains(Migration73To74))
        assertTrue(ALL_MIGRATIONS.contains(Migration74To75))
        assertTrue(ALL_MIGRATIONS.contains(Migration75To76))
        assertTrue(ALL_MIGRATIONS.contains(Migration76To77))
    }
}
