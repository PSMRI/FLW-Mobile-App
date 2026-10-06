package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KeyUtilsTest {

    @Test
    fun `accessing KeyUtils without the native library fails safely instead of returning a value`() {
        val error = try {
            KeyUtils.encryptedPassKey()
            null
        } catch (t: Throwable) {
            t
        }

        assertTrue(
            "Expected KeyUtils access to fail without the native 'sakhi' library, but it succeeded",
            error != null
        )

        val isExpectedFailure = when (error) {
            is RuntimeException -> {
                val cause = error.cause
                (error.message?.contains("sakhi") == true) && cause is UnsatisfiedLinkError
            }
            is NoClassDefFoundError -> true
            is ExceptionInInitializerError -> error.cause is RuntimeException
            else -> false
        }

        if (!isExpectedFailure) {
            fail("Unexpected failure type/content when loading KeyUtils: $error")
        }
    }
}
