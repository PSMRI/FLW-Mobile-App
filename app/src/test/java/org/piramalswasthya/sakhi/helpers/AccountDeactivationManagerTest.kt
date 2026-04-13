package org.piramalswasthya.sakhi.helpers

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDeactivationManagerTest {

    private lateinit var manager: AccountDeactivationManager

    @Before
    fun setUp() {
        manager = AccountDeactivationManager()
    }

    // =====================================================
    // emitIfCooldownPassed() Tests
    // =====================================================

    @Test
    fun `first call emits event`() = runTest {
        manager.deactivationEvent.test {
            manager.emitIfCooldownPassed("Account deactivated")

            assertEquals("Account deactivated", awaitItem())
        }
    }

    @Test
    fun `second call within cooldown is suppressed`() = runTest {
        manager.deactivationEvent.test {
            manager.emitIfCooldownPassed("Account deactivated")
            awaitItem()

            // Immediate second call — within 5 min cooldown
            manager.emitIfCooldownPassed("Account locked")

            expectNoEvents()
        }
    }

    @Test
    fun `rapid calls emit only first`() = runTest {
        manager.deactivationEvent.test {
            manager.emitIfCooldownPassed("first")
            manager.emitIfCooldownPassed("second")
            manager.emitIfCooldownPassed("third")

            assertEquals("first", awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `different error messages within cooldown are suppressed`() = runTest {
        manager.deactivationEvent.test {
            manager.emitIfCooldownPassed("Account deactivated by admin")
            awaitItem()

            manager.emitIfCooldownPassed("Account locked due to policy")

            expectNoEvents()
        }
    }

    @Test
    fun `empty error message is still emitted`() = runTest {
        manager.deactivationEvent.test {
            manager.emitIfCooldownPassed("")

            assertEquals("", awaitItem())
        }
    }
}
