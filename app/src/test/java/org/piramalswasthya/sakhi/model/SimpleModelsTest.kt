package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleModelsTest {

    // =====================================================
    // InputType Enum Tests
    // =====================================================

    @Test fun `InputType has all expected values`() {
        assertEquals(14, InputType.values().size)
    }

    @Test fun `InputType EDIT_TEXT exists`() { assertNotNull(InputType.EDIT_TEXT) }
    @Test fun `InputType DROPDOWN exists`() { assertNotNull(InputType.DROPDOWN) }
    @Test fun `InputType RADIO exists`() { assertNotNull(InputType.RADIO) }
    @Test fun `InputType DATE_PICKER exists`() { assertNotNull(InputType.DATE_PICKER) }
    @Test fun `InputType TEXT_VIEW exists`() { assertNotNull(InputType.TEXT_VIEW) }
    @Test fun `InputType IMAGE_VIEW exists`() { assertNotNull(InputType.IMAGE_VIEW) }
    @Test fun `InputType CHECKBOXES exists`() { assertNotNull(InputType.CHECKBOXES) }
    @Test fun `InputType TIME_PICKER exists`() { assertNotNull(InputType.TIME_PICKER) }
    @Test fun `InputType HEADLINE exists`() { assertNotNull(InputType.HEADLINE) }
    @Test fun `InputType AGE_PICKER exists`() { assertNotNull(InputType.AGE_PICKER) }
    @Test fun `InputType FILE_UPLOAD exists`() { assertNotNull(InputType.FILE_UPLOAD) }
    @Test fun `InputType MULTIFILE_UPLOAD exists`() { assertNotNull(InputType.MULTIFILE_UPLOAD) }
    @Test fun `InputType BUTTON exists`() { assertNotNull(InputType.BUTTON) }
    @Test fun `InputType NUMBER_PICKER exists`() { assertNotNull(InputType.NUMBER_PICKER) }

    @Test fun `InputType valueOf works`() {
        assertEquals(InputType.EDIT_TEXT, InputType.valueOf("EDIT_TEXT"))
    }

    // =====================================================
    // LogLevel Enum Tests
    // =====================================================

    @Test fun `LogLevel has 4 values`() { assertEquals(4, LogLevel.values().size) }
    @Test fun `LogLevel DEBUG exists`() { assertNotNull(LogLevel.DEBUG) }
    @Test fun `LogLevel INFO exists`() { assertNotNull(LogLevel.INFO) }
    @Test fun `LogLevel WARN exists`() { assertNotNull(LogLevel.WARN) }
    @Test fun `LogLevel ERROR exists`() { assertNotNull(LogLevel.ERROR) }

    // =====================================================
    // BottleItem Tests
    // =====================================================

    @Test fun `BottleItem creation`() {
        val item = BottleItem(1, "B001", "14-04-2026")
        assertEquals(1, item.srNo)
        assertEquals("B001", item.bottleNumber)
        assertEquals("14-04-2026", item.dateOfProvision)
    }

    @Test fun `BottleItem equals`() {
        val a = BottleItem(1, "B001", "14-04-2026")
        val b = BottleItem(1, "B001", "14-04-2026")
        assertEquals(a, b)
    }

    @Test fun `BottleItem not equals`() {
        val a = BottleItem(1, "B001", "14-04-2026")
        val b = BottleItem(2, "B002", "15-04-2026")
        assertNotEquals(a, b)
    }

    // =====================================================
    // FailedWorkerInfo Tests
    // =====================================================

    @Test fun `FailedWorkerInfo creation`() {
        val info = FailedWorkerInfo("SyncWorker", "Timeout")
        assertEquals("SyncWorker", info.workerName)
        assertEquals("Timeout", info.error)
    }

    @Test fun `FailedWorkerInfo equals`() {
        val a = FailedWorkerInfo("W1", "E1")
        val b = FailedWorkerInfo("W1", "E1")
        assertEquals(a, b)
    }

    // =====================================================
    // SyncLogEntry Tests
    // =====================================================

    @Test fun `SyncLogEntry creation`() {
        val entry = SyncLogEntry(1L, 1000L, LogLevel.INFO, "TAG", "message")
        assertEquals(1L, entry.id)
        assertEquals(1000L, entry.timestamp)
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("TAG", entry.tag)
        assertEquals("message", entry.message)
    }

    @Test fun `SyncLogEntry copy changes level`() {
        val entry = SyncLogEntry(1L, 1000L, LogLevel.INFO, "TAG", "msg")
        val copy = entry.copy(level = LogLevel.ERROR)
        assertEquals(LogLevel.ERROR, copy.level)
    }

    // =====================================================
    // HomeVisitUiState Tests
    // =====================================================

    @Test fun `HomeVisitUiState creation`() {
        val state = HomeVisitUiState(true, false)
        assertTrue(state.canAddHomeVisit)
        assertFalse(state.canViewHomeVisit)
    }

    @Test fun `HomeVisitUiState equals`() {
        val a = HomeVisitUiState(true, true)
        val b = HomeVisitUiState(true, true)
        assertEquals(a, b)
    }

    @Test fun `HomeVisitUiState copy`() {
        val state = HomeVisitUiState(true, false)
        val copy = state.copy(canViewHomeVisit = true)
        assertTrue(copy.canViewHomeVisit)
    }

    // =====================================================
    // UploadResponse Tests
    // =====================================================

    @Test fun `UploadResponse creation`() {
        val data = UploadData("OK")
        val response = UploadResponse(data, 200, "", "Success")
        assertEquals(200, response.statusCode)
        assertEquals("Success", response.status)
        assertEquals("OK", response.data?.response)
    }

    @Test fun `UploadResponse with null data`() {
        val response = UploadResponse(null, 500, "Error", "Failed")
        assertNull(response.data)
        assertEquals("Error", response.errorMessage)
    }

    @Test fun `UploadData creation`() {
        val data = UploadData("response_text")
        assertEquals("response_text", data.response)
    }

    // =====================================================
    // ImmunizationIcon Tests
    // =====================================================

    @Test fun `ImmunizationIcon creation with defaults`() {
        val icon = ImmunizationIcon(1L, 2L, "BCG", 3)
        assertEquals(1L, icon.benId)
        assertEquals(2L, icon.hhId)
        assertEquals("BCG", icon.title)
        assertEquals(3, icon.count)
        assertEquals(5, icon.maxCount)
    }

    @Test fun `ImmunizationIcon creation with custom maxCount`() {
        val icon = ImmunizationIcon(1L, 2L, "OPV", 2, 3)
        assertEquals(3, icon.maxCount)
    }

    // =====================================================
    // ChildOption Tests
    // =====================================================

    @Test fun `ChildOption creation with defaults`() {
        val option = ChildOption("FORM1", "Title", "Desc")
        assertEquals("FORM1", option.formType)
        assertEquals("Title", option.title)
        assertEquals("Desc", option.description)
        assertFalse(option.isViewMode)
        assertNull(option.visitDay)
        assertNull(option.formDataJson)
        assertNull(option.recordId)
    }

    @Test fun `ChildOption creation with all fields`() {
        val option = ChildOption("FORM1", "Title", "Desc", true, "Day 1", "{}", 42, true)
        assertTrue(option.isViewMode)
        assertEquals("Day 1", option.visitDay)
        assertEquals("{}", option.formDataJson)
        assertEquals(42, option.recordId)
        assertEquals(true, option.isIFA)
    }

    @Test fun `ChildOption equals`() {
        val a = ChildOption("F", "T", "D")
        val b = ChildOption("F", "T", "D")
        assertEquals(a, b)
    }

    @Test fun `ChildOption copy`() {
        val option = ChildOption("F", "T", "D")
        val copy = option.copy(isViewMode = true)
        assertTrue(copy.isViewMode)
    }

    // =====================================================
    // AadhaarConsentModel Tests
    // =====================================================

    @Test fun `AadhaarConsentModel creation`() {
        val model = AadhaarConsentModel("I consent")
        assertEquals("I consent", model.title)
        assertFalse(model.checked)
    }

    @Test fun `AadhaarConsentModel checked can be set`() {
        val model = AadhaarConsentModel("Consent", true)
        assertTrue(model.checked)
    }

    @Test fun `AadhaarConsentModel mutable checked`() {
        val model = AadhaarConsentModel("Consent")
        model.checked = true
        assertTrue(model.checked)
    }
}
