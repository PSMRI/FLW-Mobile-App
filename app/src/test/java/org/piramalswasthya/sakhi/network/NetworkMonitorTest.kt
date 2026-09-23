package org.piramalswasthya.sakhi.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import app.cash.turbine.test
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NetworkMonitorTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var activeNetwork: Network
    private lateinit var networkCapabilities: NetworkCapabilities
    private lateinit var fakeRequest: NetworkRequest
    private val callbackSlot: CapturingSlot<ConnectivityManager.NetworkCallback> = slot()

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        activeNetwork = mockk(relaxed = true)
        networkCapabilities = mockk(relaxed = true)
        fakeRequest = mockk(relaxed = true)

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns activeNetwork
        every { connectivityManager.getNetworkCapabilities(activeNetwork) } returns networkCapabilities
        every { connectivityManager.registerNetworkCallback(any(), capture(callbackSlot)) } just Runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

        mockkConstructor(NetworkRequest.Builder::class)
        every { anyConstructed<NetworkRequest.Builder>().addCapability(any()) } answers { self as NetworkRequest.Builder }
        every { anyConstructed<NetworkRequest.Builder>().build() } returns fakeRequest
    }

    @After
    fun tearDown() {
        unmockkConstructor(NetworkRequest.Builder::class)
    }

    @Test
    fun `observeConnectivity emits true when the active network already has internet`() = runTest {
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        NetworkMonitor.observeConnectivity(context).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { connectivityManager.registerNetworkCallback(fakeRequest, any<ConnectivityManager.NetworkCallback>()) }
    }

    @Test
    fun `observeConnectivity emits false when there is no active network capability`() = runTest {
        every { connectivityManager.getNetworkCapabilities(activeNetwork) } returns null

        NetworkMonitor.observeConnectivity(context).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeConnectivity emits true after the callback reports the network is available`() = runTest {
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        NetworkMonitor.observeConnectivity(context).test {
            assertFalse(awaitItem())
            callbackSlot.captured.onAvailable(activeNetwork)
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeConnectivity emits false after the callback reports the network is lost`() = runTest {
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        NetworkMonitor.observeConnectivity(context).test {
            assertTrue(awaitItem())
            callbackSlot.captured.onLost(activeNetwork)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeConnectivity does not re-emit consecutive duplicate values`() = runTest {
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        NetworkMonitor.observeConnectivity(context).test {
            assertTrue(awaitItem())
            callbackSlot.captured.onAvailable(activeNetwork)
            callbackSlot.captured.onLost(activeNetwork)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeConnectivity unregisters the callback when the flow is cancelled`() = runTest {
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        NetworkMonitor.observeConnectivity(context).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { connectivityManager.unregisterNetworkCallback(callbackSlot.captured) }
    }
}
