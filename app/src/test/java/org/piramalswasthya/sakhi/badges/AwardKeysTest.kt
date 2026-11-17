package org.piramalswasthya.sakhi.badges

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.badges.domain.AwardKeys

/**
 * The award key has to be two things at once, and both fail silently if they break.
 *
 * If it stops being deterministic, the uniqueness constraint stops matching and a restored
 * award duplicates the one evaluation derives — visible only as a badge counted twice. If it
 * stops being opaque, a beneficiary identifier starts leaving the device, which is visible
 * nowhere at all.
 */
class AwardKeysTest {

    @Test
    fun `the same case always produces the same key`() {
        assertEquals(AwardKeys.forCase("12345"), AwardKeys.forCase("12345"))
    }

    @Test
    fun `different cases produce different keys`() {
        assertNotEquals(AwardKeys.forCase("12345"), AwardKeys.forCase("12346"))
    }

    /** Surrounding whitespace is a formatting difference, not a different beneficiary. */
    @Test
    fun `keys ignore surrounding whitespace`() {
        assertEquals(AwardKeys.forCase("12345"), AwardKeys.forCase("  12345 "))
    }

    /** The identifier must not be readable in, or recoverable from, what is sent. */
    @Test
    fun `the key does not contain the case id`() {
        val key = AwardKeys.forCase("9876543210")
        assertFalse(key.contains("9876543210"))
        assertEquals(32, key.length)
        assertTrue("not hex: $key", key.all { it in "0123456789abcdef" })
    }

    /** Streak and cumulative badges carry no case, and must stay on the empty key. */
    @Test
    fun `a blank case is an empty key`() {
        assertEquals("", AwardKeys.forCase(""))
        assertEquals("", AwardKeys.forCase("   "))
    }
}
