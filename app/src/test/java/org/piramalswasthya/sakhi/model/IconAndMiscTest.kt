package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test

class IconAndMiscTest {

    // =====================================================
    // BottleItem Tests
    // =====================================================

    @Test fun `BottleItem can be created`() {
        val item = BottleItem(srNo = 1, bottleNumber = "B001", dateOfProvision = "2025-01-01")
        assertNotNull(item)
    }

    @Test fun `BottleItem srNo is correct`() {
        val item = BottleItem(srNo = 5, bottleNumber = "B005", dateOfProvision = "2025-03-15")
        assertEquals(5, item.srNo)
        assertEquals("B005", item.bottleNumber)
    }

    @Test fun `BottleItem equality`() {
        val a = BottleItem(srNo = 1, bottleNumber = "B001", dateOfProvision = "2025-01-01")
        val b = BottleItem(srNo = 1, bottleNumber = "B001", dateOfProvision = "2025-01-01")
        assertEquals(a, b)
    }

    @Test fun `BottleItem inequality`() {
        val a = BottleItem(srNo = 1, bottleNumber = "B001", dateOfProvision = "2025-01-01")
        val b = BottleItem(srNo = 2, bottleNumber = "B002", dateOfProvision = "2025-01-02")
        assertNotEquals(a, b)
    }

    @Test fun `BottleItem copy works`() {
        val item = BottleItem(srNo = 1, bottleNumber = "B001", dateOfProvision = "2025-01-01")
        val copy = item.copy(bottleNumber = "B999")
        assertEquals("B999", copy.bottleNumber)
        assertEquals(1, copy.srNo)
    }

    // =====================================================
    // ChildOption Tests
    // =====================================================

    @Test fun `ChildOption can be created`() {
        val opt = ChildOption(formType = "HBNC", title = "HBNC Form", description = "Day 1")
        assertNotNull(opt)
    }

    @Test fun `ChildOption default isViewMode is false`() {
        val opt = ChildOption(formType = "HBNC", title = "HBNC", description = "Desc")
        assertFalse(opt.isViewMode)
    }

    @Test fun `ChildOption equality`() {
        val a = ChildOption(formType = "HBNC", title = "HBNC", description = "Day 1")
        val b = ChildOption(formType = "HBNC", title = "HBNC", description = "Day 1")
        assertEquals(a, b)
    }

    @Test fun `ChildOption inequality`() {
        val a = ChildOption(formType = "HBNC", title = "HBNC", description = "Day 1")
        val b = ChildOption(formType = "HBYC", title = "HBYC", description = "Month 1")
        assertNotEquals(a, b)
    }

    @Test fun `ChildOption copy with viewMode`() {
        val opt = ChildOption(formType = "HBNC", title = "HBNC", description = "Day 1")
        val copy = opt.copy(isViewMode = true)
        assertTrue(copy.isViewMode)
    }

    // =====================================================
    // FailedWorkerInfo Tests
    // =====================================================

    @Test fun `FailedWorkerInfo can be created`() {
        val info = FailedWorkerInfo(workerName = "syncWorker", error = "timeout")
        assertNotNull(info)
    }

    @Test fun `FailedWorkerInfo fields are correct`() {
        val info = FailedWorkerInfo(workerName = "pushWorker", error = "network error")
        assertEquals("pushWorker", info.workerName)
        assertEquals("network error", info.error)
    }

    @Test fun `FailedWorkerInfo equality`() {
        val a = FailedWorkerInfo(workerName = "sync", error = "fail")
        val b = FailedWorkerInfo(workerName = "sync", error = "fail")
        assertEquals(a, b)
    }

    @Test fun `FailedWorkerInfo inequality`() {
        val a = FailedWorkerInfo(workerName = "sync", error = "fail")
        val b = FailedWorkerInfo(workerName = "push", error = "timeout")
        assertNotEquals(a, b)
    }

    @Test fun `FailedWorkerInfo copy works`() {
        val info = FailedWorkerInfo(workerName = "sync", error = "fail")
        val copy = info.copy(error = "success")
        assertEquals("success", copy.error)
    }

    // =====================================================
    // PreviewItem Tests
    // =====================================================

    @Test fun `PreviewItem can be created`() {
        val item = PreviewItem(label = "Name", value = "John")
        assertNotNull(item)
    }

    @Test fun `PreviewItem default isImage is false`() {
        val item = PreviewItem(label = "Name", value = "John")
        assertFalse(item.isImage)
    }

    @Test fun `PreviewItem default imageUri is null`() {
        val item = PreviewItem(label = "Name", value = "John")
        assertNull(item.imageUri)
    }

    @Test fun `PreviewItem equality`() {
        val a = PreviewItem(label = "Name", value = "John")
        val b = PreviewItem(label = "Name", value = "John")
        assertEquals(a, b)
    }

    @Test fun `PreviewItem inequality`() {
        val a = PreviewItem(label = "Name", value = "John")
        val b = PreviewItem(label = "Name", value = "Jane")
        assertNotEquals(a, b)
    }

    @Test fun `PreviewItem copy works`() {
        val item = PreviewItem(label = "Name", value = "John")
        val copy = item.copy(value = "Jane", isImage = true)
        assertEquals("Jane", copy.value)
        assertTrue(copy.isImage)
    }

    // =====================================================
    // InputType Enum Tests
    // =====================================================

    @Test fun `InputType EDIT_TEXT exists`() { assertNotNull(InputType.EDIT_TEXT) }
    @Test fun `InputType DATE_PICKER exists`() { assertNotNull(InputType.DATE_PICKER) }
    @Test fun `InputType DROPDOWN exists`() { assertNotNull(InputType.DROPDOWN) }
    @Test fun `InputType RADIO exists`() { assertNotNull(InputType.RADIO) }
    @Test fun `InputType HEADLINE exists`() { assertNotNull(InputType.HEADLINE) }
    @Test fun `InputType CHECKBOXES exists`() { assertNotNull(InputType.CHECKBOXES) }
    @Test fun `InputType TEXT_VIEW exists`() { assertNotNull(InputType.TEXT_VIEW) }
    @Test fun `InputType TIME_PICKER exists`() { assertNotNull(InputType.TIME_PICKER) }
    @Test fun `InputType IMAGE_VIEW exists`() { assertNotNull(InputType.IMAGE_VIEW) }

    // =====================================================
    // Gender Enum Tests (extended)
    // =====================================================

    @Test fun `Gender MALE name`() { assertEquals("MALE", Gender.MALE.name) }
    @Test fun `Gender FEMALE name`() { assertEquals("FEMALE", Gender.FEMALE.name) }
    @Test fun `Gender TRANSGENDER name`() { assertEquals("TRANSGENDER", Gender.TRANSGENDER.name) }
    @Test fun `Gender valueOf MALE`() { assertEquals(Gender.MALE, Gender.valueOf("MALE")) }
    @Test fun `Gender valueOf FEMALE`() { assertEquals(Gender.FEMALE, Gender.valueOf("FEMALE")) }
    @Test fun `Gender has 3 values`() { assertEquals(3, Gender.values().size) }

    // =====================================================
    // AgeUnit Enum Tests (extended)
    // =====================================================

    @Test fun `AgeUnit YEARS exists`() { assertNotNull(AgeUnit.YEARS) }
    @Test fun `AgeUnit MONTHS exists`() { assertNotNull(AgeUnit.MONTHS) }
    @Test fun `AgeUnit DAYS exists`() { assertNotNull(AgeUnit.DAYS) }
    @Test fun `AgeUnit has 3 values`() { assertEquals(3, AgeUnit.values().size) }
    @Test fun `AgeUnit valueOf YEARS`() { assertEquals(AgeUnit.YEARS, AgeUnit.valueOf("YEARS")) }

    // =====================================================
    // BenStatus Enum Tests (extended)
    // =====================================================

    @Test fun `BenStatus values exist`() { assertTrue(BenStatus.values().isNotEmpty()) }
    @Test fun `BenStatus valueOf first value works`() {
        val first = BenStatus.values()[0]
        assertEquals(first, BenStatus.valueOf(first.name))
    }

    // =====================================================
    // LogLevel Enum Tests
    // =====================================================

    @Test fun `LogLevel DEBUG exists`() { assertNotNull(LogLevel.DEBUG) }
    @Test fun `LogLevel INFO exists`() { assertNotNull(LogLevel.INFO) }
    @Test fun `LogLevel WARN exists`() { assertNotNull(LogLevel.WARN) }
    @Test fun `LogLevel ERROR exists`() { assertNotNull(LogLevel.ERROR) }
    @Test fun `LogLevel has 4 values`() { assertEquals(4, LogLevel.values().size) }
    @Test fun `LogLevel valueOf DEBUG`() { assertEquals(LogLevel.DEBUG, LogLevel.valueOf("DEBUG")) }
    @Test fun `LogLevel valueOf ERROR`() { assertEquals(LogLevel.ERROR, LogLevel.valueOf("ERROR")) }
}
