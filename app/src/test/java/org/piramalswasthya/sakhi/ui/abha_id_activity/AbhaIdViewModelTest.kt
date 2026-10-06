package org.piramalswasthya.sakhi.ui.abha_id_activity

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AbhaTokenResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import io.mockk.every

@OptIn(ExperimentalCoroutinesApi::class)
class AbhaIdViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var prefDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var viewModel: AbhaIdViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val user = mockk<User>(relaxed = true)
        every { prefDao.getLoggedInUser() } returns user
        viewModel = AbhaIdViewModel(abhaIdRepo, prefDao, userRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `state is not null after init`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `errorMessage is not null after init`() {
        assertNotNull(viewModel.errorMessage)
    }

    private fun tokenResponse(accessToken: String = "acc-tok") = AbhaTokenResponse(
        accessToken = accessToken,
        expiresIn = 3600,
        refreshExpiresIn = 7200,
        refreshToken = "ref-tok",
        tokenType = "Bearer"
    )

    // =====================================================
    // generateAmritToken() Tests
    // =====================================================

    @Test
    fun `generateAmritToken does nothing when no user is logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null
        val vm = AbhaIdViewModel(abhaIdRepo, prefDao, userRepo)

        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.LOADING_TOKEN, vm.state.value)
        coVerify(exactly = 0) { abhaIdRepo.getAccessToken() }
    }

    @Test
    fun `generateAmritToken sets ERROR_SERVER when refreshTokenTmc fails`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        val user = mockk<User>(relaxed = true)
        every { prefDao.getLoggedInUser() } returns user
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        val vm = AbhaIdViewModel(abhaIdRepo, prefDao, userRepo)

        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.ERROR_SERVER, vm.state.value)
        coVerify(exactly = 0) { abhaIdRepo.getAccessToken() }
    }

    @Test
    fun `generateAmritToken proceeds to generateAccessToken when refreshTokenTmc succeeds`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { prefDao.getLoggedInUser() } returns user
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Success(tokenResponse())
        every { prefDao.getPublicKeyForAbha() } returns "existing-key"
        val vm = AbhaIdViewModel(abhaIdRepo, prefDao, userRepo)

        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.SUCCESS, vm.state.value)
    }

    // =====================================================
    // generateAccessToken() Tests
    // =====================================================

    @Test
    fun `generateAccessToken sets SUCCESS and skips cert fetch when public key already present`() =
        runTest {
            every { prefDao.getPublicKeyForAbha() } returns "existing-key"
            coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Success(tokenResponse("abc"))

            viewModel.generateAccessToken()
            advanceUntilIdle()

            assertEquals(AbhaIdViewModel.State.SUCCESS, viewModel.state.value)
            coVerify(exactly = 0) { abhaIdRepo.getAuthCert() }
        }

    @Test
    fun `generateAccessToken fetches and saves public key when missing`() = runTest {
        every { prefDao.getPublicKeyForAbha() } returns null
        coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Success(tokenResponse())
        coEvery { abhaIdRepo.getAuthCert() } returns NetworkResult.Success("pub-key-123")

        viewModel.generateAccessToken()
        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.SUCCESS, viewModel.state.value)
        coVerify { prefDao.savePublicKeyForAbha("pub-key-123") }
    }

    @Test
    fun `generateAccessToken leaves public key unset when getAuthCert returns error`() = runTest {
        every { prefDao.getPublicKeyForAbha() } returns null
        coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Success(tokenResponse())
        coEvery { abhaIdRepo.getAuthCert() } returns NetworkResult.Error(500, "cert error")

        viewModel.generateAccessToken()
        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.SUCCESS, viewModel.state.value)
        coVerify(exactly = 0) { prefDao.savePublicKeyForAbha(any()) }
    }

    @Test
    fun `generateAccessToken leaves public key unset when getAuthCert returns network error`() =
        runTest {
            every { prefDao.getPublicKeyForAbha() } returns null
            coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Success(tokenResponse())
            coEvery { abhaIdRepo.getAuthCert() } returns NetworkResult.NetworkError

            viewModel.generateAccessToken()
            advanceUntilIdle()

            assertEquals(AbhaIdViewModel.State.SUCCESS, viewModel.state.value)
            coVerify(exactly = 0) { prefDao.savePublicKeyForAbha(any()) }
        }

    @Test
    fun `generateAccessToken sets ERROR_SERVER and errorMessage on error`() = runTest {
        coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.Error(500, "token fetch failed")

        viewModel.generateAccessToken()
        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("token fetch failed", viewModel.errorMessage.value)
    }

    @Test
    fun `generateAccessToken sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.getAccessToken() } returns NetworkResult.NetworkError

        viewModel.generateAccessToken()
        advanceUntilIdle()

        assertEquals(AbhaIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }
}
