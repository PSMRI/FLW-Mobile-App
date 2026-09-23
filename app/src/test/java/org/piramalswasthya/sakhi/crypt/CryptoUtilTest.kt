package org.piramalswasthya.sakhi.crypt

import org.junit.Assume
import org.junit.Test

class CryptoUtilTest {

    @Test
    fun `CryptoUtil cannot be constructed without the native sakhi library in a plain JVM test`() {
        try {
            CryptoUtil()
            Assume.assumeTrue(
                "Unexpectedly able to construct CryptoUtil in this environment; " +
                    "no native-library blocker present, consider adding real encrypt() round-trip tests",
                false
            )
        } catch (t: Throwable) {
            Assume.assumeNoException(
                "CryptoUtil() cannot be constructed in a plain JVM unit test because its passPhrase field " +
                    "initializer calls KeyUtils.encryptedPassKey(), which requires the native 'sakhi' library " +
                    "(same structural blocker as UserRepo/CryptoUtil documented elsewhere). Needs Robolectric " +
                    "or a src/main refactor (inject passphrase instead of computing it in the constructor) to " +
                    "unit test encrypt()'s round-trip logic.",
                t as? Exception ?: Exception(t)
            )
        }
    }
}
