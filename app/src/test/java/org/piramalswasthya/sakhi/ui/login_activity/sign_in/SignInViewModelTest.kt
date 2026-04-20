package org.piramalswasthya.sakhi.ui.login_activity.sign_in

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UserRepo

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var pref: PreferenceDao

    private lateinit var viewModel: SignInViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { pref.getLoggedInUser() } returns null
        every { userRepo.unProcessedRecordCount } returns flowOf(emptyList())
        viewModel = SignInViewModel(userRepo, database, pref)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.state.value is NetworkResponse.Idle)
    }

    @Test
    fun `loggedInUser is null when no user logged in`() {
        assertNull(viewModel.loggedInUser.value)
    }

    @Test
    fun `loggedInUser is set from preferences on init`() {
        val user = mockk<User>()
        every { pref.getLoggedInUser() } returns user
        val vm = SignInViewModel(userRepo, database, pref)
        assertEquals(user, vm.loggedInUser.value)
    }

    @Test
    fun `logoutComplete is initially null`() {
        assertNull(viewModel.logoutComplete.value)
    }

    @Test
    fun `unprocessedRecordsCount is initially 0`() {
        assertEquals(0, viewModel.unprocessedRecordsCount.value)
    }

    // =====================================================
    // loginInClicked() Tests
    // =====================================================

    @Test
    fun `loginInClicked sets state to Loading`() {
        viewModel.loginInClicked()
        assertTrue(viewModel.state.value is NetworkResponse.Loading)
    }

    // =====================================================
    // authUser() Tests
    // =====================================================

    @Test
    fun `authUser sets state to Success on successful authentication`() = runTest {
        val user = mockk<User>()
        coEvery { userRepo.authenticateUser("admin", "pass123") } returns NetworkResponse.Success(user)

        viewModel.authUser("admin", "pass123")
        advanceUntilIdle()

        assertTrue(viewModel.state.value is NetworkResponse.Success)
        assertEquals(user, viewModel.state.value?.data)
    }

    @Test
    fun `authUser sets state to Error on exception`() = runTest {
        coEvery { userRepo.authenticateUser("admin", "bad") } throws RuntimeException("timeout")

        viewModel.authUser("admin", "bad")
        advanceUntilIdle()

        assertTrue(viewModel.state.value is NetworkResponse.Error)
        verify { pref.deleteLoginCred() }
    }

    // =====================================================
    // getLoggedInUser() Tests
    // =====================================================

    @Test
    fun `getLoggedInUser returns user from preferences`() {
        val user = mockk<User>()
        every { pref.getLoggedInUser() } returns user
        assertEquals(user, viewModel.getLoggedInUser())
    }

    @Test
    fun `getLoggedInUser returns null when no user`() {
        every { pref.getLoggedInUser() } returns null
        assertNull(viewModel.getLoggedInUser())
    }

    // =====================================================
    // fetchRemembered* Tests
    // =====================================================

    @Test
    fun `fetchRememberedUserName returns saved username`() {
        every { pref.getRememberedUserName() } returns "testUser"
        assertEquals("testUser", viewModel.fetchRememberedUserName())
    }

    @Test
    fun `fetchRememberedUserName returns null when not set`() {
        every { pref.getRememberedUserName() } returns null
        assertNull(viewModel.fetchRememberedUserName())
    }

    @Test
    fun `fetchRememberedPassword returns saved password`() {
        every { pref.getRememberedPassword() } returns "pass123"
        assertEquals("pass123", viewModel.fetchRememberedPassword())
    }

    @Test
    fun `fetchRememberedPassword returns null when not set`() {
        every { pref.getRememberedPassword() } returns null
        assertNull(viewModel.fetchRememberedPassword())
    }

    @Test
    fun `fetchRememberedState returns saved state`() {
        every { pref.getRememberedState() } returns "Bihar"
        assertEquals("Bihar", viewModel.fetchRememberedState())
    }

    @Test
    fun `fetchRememberedState returns null when not set`() {
        every { pref.getRememberedState() } returns null
        assertNull(viewModel.fetchRememberedState())
    }

    // =====================================================
    // rememberUser() Tests
    // =====================================================

    @Test
    fun `rememberUser calls pref registerLoginCred`() = runTest {
        coEvery { pref.registerLoginCred("user1", "pass1") } returns Unit

        viewModel.rememberUser("user1", "pass1")
        advanceUntilIdle()
        testDispatcher.scheduler.runCurrent()
        Thread.sleep(100)
        testDispatcher.scheduler.runCurrent()

        coVerify { pref.registerLoginCred("user1", "pass1") }
    }

    // =====================================================
    // forgetUser() Tests
    // =====================================================

    @Test
    fun `forgetUser calls pref deleteLoginCred`() = runTest {
        coEvery { pref.deleteLoginCred() } returns Unit

        viewModel.forgetUser()
        advanceUntilIdle()
        testDispatcher.scheduler.runCurrent()
        Thread.sleep(100)
        testDispatcher.scheduler.runCurrent()

        coVerify { pref.deleteLoginCred() }
    }

    // =====================================================
    // updateState() Tests
    // =====================================================

    @Test
    fun `updateState sets state to provided value`() {
        val newState = NetworkResponse.Loading<User?>()
        viewModel.updateState(newState)
        assertTrue(viewModel.state.value is NetworkResponse.Loading)
    }

    @Test
    fun `updateState sets Error state`() {
        val errorState = NetworkResponse.Error<User?>("error msg")
        viewModel.updateState(errorState)
        assertTrue(viewModel.state.value is NetworkResponse.Error)
        assertEquals("error msg", viewModel.state.value?.message)
    }

    // =====================================================
    // authenticateForMigration() Tests
    // =====================================================

    @Test
    fun `authenticateForMigration returns Success on valid credentials`() = runTest {
        val user = mockk<User>()
        coEvery { userRepo.authenticateUser("admin", "pass") } returns NetworkResponse.Success(user)

        val result = viewModel.authenticateForMigration("admin", "pass")

        assertTrue(result is NetworkResponse.Success)
        assertEquals(user, result.data)
    }

    @Test
    fun `authenticateForMigration returns Error on invalid credentials`() = runTest {
        coEvery { userRepo.authenticateUser("admin", "wrong") } returns NetworkResponse.Error("Invalid")

        val result = viewModel.authenticateForMigration("admin", "wrong")

        assertTrue(result is NetworkResponse.Error)
    }
}
