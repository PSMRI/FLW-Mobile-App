package org.piramalswasthya.sakhi.ui.abha_id_activity

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
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
}
