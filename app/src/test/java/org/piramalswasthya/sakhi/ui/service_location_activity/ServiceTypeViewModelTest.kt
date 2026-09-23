package org.piramalswasthya.sakhi.ui.service_location_activity

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceTypeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao

    private lateinit var viewModel: ServiceTypeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = ServiceTypeViewModel(pref)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is LOADING`() {
        assertEquals(ServiceTypeViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `isLocationSet returns false while loading`() {
        assertFalse(viewModel.isLocationSet())
    }

    @Test
    fun `isNoUserFound live data is not null`() {
        assertNotNull(viewModel.isNoUserFound)
    }

    @Test
    fun `selectedVillage is null before location is resolved`() {
        assertNull(viewModel.selectedVillage)
    }

    @Test
    fun `selectedVillageName is null for ENGLISH when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for HINDI when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for ASSAMESE when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for BANGLA when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        assertNull(viewModel.selectedVillageName)
    }

    @Test(expected = Throwable::class)
    fun `setVillage before user is resolved throws`() {
        viewModel.setVillage(0)
    }

    @Test(expected = Throwable::class)
    fun `saveCurrentLocation before user is resolved throws`() {
        viewModel.saveCurrentLocation()
    }

    private fun buildUser() = User(
        userId = 1,
        name = "Asha Worker",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 1,
        state = LocationEntity(1, "State", "StateHi", "StateAs", "StateBn"),
        district = LocationEntity(2, "District", "DistrictHi", "DistrictAs", "DistrictBn"),
        block = LocationEntity(3, "Block", "BlockHi", "BlockAs", "BlockBn"),
        villages = listOf(LocationEntity(4, "Village", "VillageHi", "VillageAs", "VillageBn"))
    )

    @Test
    fun `init resolves user and moves state to SUCCESS for ENGLISH`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getLoggedInUser() } returns user
        every { pref.getSupervisorSubcenter() } returns "Sub Center A"
        every { pref.getLocationRecord() } returns null
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(ServiceTypeViewModel.State.SUCCESS, vm.state.value)
        assertFalse(vm.isNoUserFound.value!!)
        assertEquals("Asha Worker", vm.userName)
        assertEquals("Sub Center A", vm.facilityName)
        assertEquals("State", vm.stateDropdownEntry)
        assertEquals("District", vm.districtDropdownEntry)
        assertEquals("Block", vm.blockDropdownEntry)
        assertEquals(listOf("Village"), vm.villageList.toList())
        assertNull(vm.selectedVillageName)
        assertFalse(vm.isLocationSet())
    }

    @Test
    fun `init keeps pref overrides for state district block when non-empty for ENGLISH`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns "OverrideState"
        every { pref.getDistrict() } returns "OverrideDistrict"
        every { pref.getBlock() } returns "OverrideBlock"

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals("OverrideState", vm.stateDropdownEntry)
        assertEquals("OverrideDistrict", vm.districtDropdownEntry)
        assertEquals("OverrideBlock", vm.blockDropdownEntry)
    }

    @Test
    fun `init resolves user for HINDI using translated names`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(ServiceTypeViewModel.State.SUCCESS, vm.state.value)
        assertEquals("StateHi", vm.stateDropdownEntry)
        assertEquals("DistrictHi", vm.districtDropdownEntry)
        assertEquals("BlockHi", vm.blockDropdownEntry)
        assertEquals(listOf("VillageHi"), vm.villageList.toList())
    }

    @Test
    fun `init resolves user for ASSAMESE using translated names`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(ServiceTypeViewModel.State.SUCCESS, vm.state.value)
        assertEquals("StateAs", vm.stateDropdownEntry)
        assertEquals("DistrictAs", vm.districtDropdownEntry)
        assertEquals("BlockAs", vm.blockDropdownEntry)
        assertEquals(listOf("VillageAs"), vm.villageList.toList())
    }

    @Test
    fun `init resolves user for BANGLA using translated names`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        every { pref.getLoggedInUser() } returns user

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(ServiceTypeViewModel.State.SUCCESS, vm.state.value)
        assertEquals("StateBn", vm.stateDropdownEntry)
        assertEquals("DistrictBn", vm.districtDropdownEntry)
        assertEquals("BlockBn", vm.blockDropdownEntry)
        assertEquals(listOf("VillageBn"), vm.villageList.toList())
    }

    @Test
    fun `init falls back to english names when translations are missing`() = runTest {
        val user = User(
            userId = 1,
            name = "Asha Worker",
            userName = "asha1",
            password = "pwd",
            role = "ASHA",
            serviceMapId = 1,
            state = LocationEntity(1, "State"),
            district = LocationEntity(2, "District"),
            block = LocationEntity(3, "Block"),
            villages = listOf(LocationEntity(4, "Village"))
        )
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals("State", vm.stateDropdownEntry)
        assertEquals("District", vm.districtDropdownEntry)
        assertEquals("Block", vm.blockDropdownEntry)
        assertEquals(listOf("Village"), vm.villageList.toList())
    }

    @Test
    fun `init resolves user for ASSAMESE falls back to english names when translations are missing`() = runTest {
        val user = User(
            userId = 1,
            name = "Asha Worker",
            userName = "asha1",
            password = "pwd",
            role = "ASHA",
            serviceMapId = 1,
            state = LocationEntity(1, "State"),
            district = LocationEntity(2, "District"),
            block = LocationEntity(3, "Block"),
            villages = listOf(LocationEntity(4, "Village"))
        )
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals("State", vm.stateDropdownEntry)
        assertEquals("District", vm.districtDropdownEntry)
        assertEquals("Block", vm.blockDropdownEntry)
        assertEquals(listOf("Village"), vm.villageList.toList())
    }

    @Test
    fun `init resolves user for BANGLA falls back to english names when translations are missing`() = runTest {
        val user = User(
            userId = 1,
            name = "Asha Worker",
            userName = "asha1",
            password = "pwd",
            role = "ASHA",
            serviceMapId = 1,
            state = LocationEntity(1, "State"),
            district = LocationEntity(2, "District"),
            block = LocationEntity(3, "Block"),
            villages = listOf(LocationEntity(4, "Village"))
        )
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        every { pref.getLoggedInUser() } returns user

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals("State", vm.stateDropdownEntry)
        assertEquals("District", vm.districtDropdownEntry)
        assertEquals("Block", vm.blockDropdownEntry)
        assertEquals(listOf("Village"), vm.villageList.toList())
    }

    @Test
    fun `stateList districtList and blockList expose single-entry arrays from the resolved dropdown entries`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getLoggedInUser() } returns user
        every { pref.getState() } returns ""
        every { pref.getDistrict() } returns ""
        every { pref.getBlock() } returns ""

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(listOf("State"), vm.stateList.toList())
        assertEquals(listOf("District"), vm.districtList.toList())
        assertEquals(listOf("Block"), vm.blockList.toList())
        assertEquals(listOf("Village"), vm.villageList.toList())
    }

    @Test
    fun `init sets isNoUserFound when no logged in user exists`() = runTest {
        every { pref.getLoggedInUser() } returns null

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertEquals(ServiceTypeViewModel.State.LOADING, vm.state.value)
        assertTrue(vm.isNoUserFound.value!!)
        assertFalse(vm.isLocationSet())
    }

    @Test
    fun `isLocationSet is true once init resolves an existing location record`() = runTest {
        val user = buildUser()
        val village = LocationEntity(4, "Village", "VillageHi", "VillageAs", "VillageBn")
        val record = LocationRecord(
            LocationEntity(1, "India"),
            user.state,
            user.district,
            user.block,
            village
        )
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getLoggedInUser() } returns user
        every { pref.getLocationRecord() } returns record

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        assertTrue(vm.isLocationSet())
        assertEquals("Village", vm.selectedVillage?.name)
        assertEquals("Village", vm.selectedVillageName)
    }

    @Test
    fun `setVillage and saveCurrentLocation persist the newly selected village`() = runTest {
        val user = buildUser()
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getLoggedInUser() } returns user

        val vm = ServiceTypeViewModel(pref)
        advanceUntilIdle()

        vm.setVillage(0)
        vm.saveCurrentLocation()

        assertEquals("Village", vm.selectedVillage?.name)
        verify { pref.saveLocationRecord(any()) }
    }
}
