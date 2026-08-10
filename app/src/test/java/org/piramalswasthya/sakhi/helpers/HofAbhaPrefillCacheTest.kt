package org.piramalswasthya.sakhi.helpers

import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.FamilyMember

class HofAbhaPrefillCacheTest {

    private lateinit var cache: HofAbhaPrefillCache

    @Before
    fun setUp() {
        cache = HofAbhaPrefillCache()
    }

    @Test
    fun `put then consume returns the same member`() {
        val member = mockk<FamilyMember>()
        cache.put(householdId = 42L, member = member)
        assertSame(member, cache.consume(42L))
    }

    @Test
    fun `consume removes the entry so second consume is null`() {
        val member = mockk<FamilyMember>()
        cache.put(7L, member)
        cache.consume(7L)
        assertNull(cache.consume(7L))
    }

    @Test
    fun `consume of unknown id returns null`() {
        assertNull(cache.consume(999L))
    }

    @Test
    fun `put with draft id 0 is a no-op`() {
        val member = mockk<FamilyMember>()
        cache.put(0L, member)
        assertNull(cache.consume(0L))
    }

    @Test
    fun `multiple ids are kept independent`() {
        val a = mockk<FamilyMember>()
        val b = mockk<FamilyMember>()
        cache.put(1L, a)
        cache.put(2L, b)
        assertSame(b, cache.consume(2L))
        assertSame(a, cache.consume(1L))
    }

    @Test
    fun `put overwrites previous member for same id`() {
        val first = mockk<FamilyMember>()
        val second = mockk<FamilyMember>()
        cache.put(5L, first)
        cache.put(5L, second)
        assertSame(second, cache.consume(5L))
    }
}
