package org.piramalswasthya.sakhi.helpers

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TokenExpiryManagerTest {

    private lateinit var manager: TokenExpiryManager

    @Before
    fun setUp() {
        manager = TokenExpiryManager()
    }

    // =====================================================
    // onRefreshFailed() Tests
    // =====================================================

    @Test
    fun `single failure does not trigger logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()

            expectNoEvents()
        }
    }

    @Test
    fun `two failures do not trigger logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()

            expectNoEvents()
        }
    }

    @Test
    fun `three failures do not trigger logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()

            expectNoEvents()
        }
    }

    @Test
    fun `four failures do not trigger logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()

            expectNoEvents()
        }
    }

    @Test
    fun `five consecutive failures trigger logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()

            awaitItem()
        }
    }

    @Test
    fun `counter resets after triggering logout`() = runTest {
        manager.forceLogoutEvent.test {
            // First round: 5 failures → logout
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            awaitItem()

            // Next single failure should not trigger logout again
            manager.onRefreshFailed()
            expectNoEvents()
        }
    }

    @Test
    fun `ten consecutive failures trigger logout twice`() = runTest {
        manager.forceLogoutEvent.test {
            // First round
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            awaitItem()

            // Second round
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            awaitItem()
        }
    }

    // =====================================================
    // onRefreshSuccess() Tests
    // =====================================================

    @Test
    fun `success resets failure counter`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshSuccess() // reset

            // Need 5 more failures now, not 1
            manager.onRefreshFailed()
            expectNoEvents()
        }
    }

    @Test
    fun `success after four failures prevents logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshSuccess()
            manager.onRefreshFailed() // only 1 after reset

            expectNoEvents()
        }
    }

    @Test
    fun `success with no prior failures does not crash`() {
        manager.onRefreshSuccess()
        // No exception thrown
    }

    @Test
    fun `multiple consecutive successes do not crash`() {
        manager.onRefreshSuccess()
        manager.onRefreshSuccess()
        manager.onRefreshSuccess()
        // No exception thrown
    }

    // =====================================================
    // Mixed Scenario Tests
    // =====================================================

    @Test
    fun `fail fail success fail fail fail fail fail triggers logout`() = runTest {
        manager.forceLogoutEvent.test {
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshSuccess()

            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            manager.onRefreshFailed()
            awaitItem()
        }
    }

    @Test
    fun `alternating fail and success never triggers logout`() = runTest {
        manager.forceLogoutEvent.test {
            repeat(10) {
                manager.onRefreshFailed()
                manager.onRefreshSuccess()
            }

            expectNoEvents()
        }
    }
}
