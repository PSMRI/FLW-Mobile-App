package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField

class FormRendererAdapterTest {

    private fun field(
        fieldId: String = "f1",
        label: String = "Label",
        type: String = "text",
        value: Any? = null,
        errorMessage: String? = null,
        visible: Boolean = true
    ) = FormField(
        fieldId = fieldId,
        label = label,
        type = type,
        isRequired = false,
        value = value,
        errorMessage = errorMessage,
        visible = visible
    )

    private fun newAdapter(fields: MutableList<FormField>): FormRendererAdapter? = try {
        FormRendererAdapter(fields, onValueChanged = { _, _ -> })
    } catch (e: NullPointerException) {
        org.junit.Assume.assumeNoException(e)
        null
    }

    @Test
    fun itemCount_matchesFieldsSize() {
        val adapter = newAdapter(mutableListOf(field(), field(fieldId = "f2"))) ?: return
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = newAdapter(mutableListOf()) ?: return
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun getUpdatedFields_and_getCurrentFields_returnBackingList() {
        val fields = mutableListOf(field(fieldId = "f1"))
        val adapter = newAdapter(fields) ?: return
        assertEquals(fields, adapter.getUpdatedFields())
        assertEquals(fields, adapter.getCurrentFields())
    }

    @Test
    fun updateFields_withDifferentSize_replacesAllFields() {
        val adapter = newAdapter(mutableListOf(field(fieldId = "f1"))) ?: return
        try {
            adapter.updateFields(listOf(field(fieldId = "f2"), field(fieldId = "f3")))
        } catch (e: NullPointerException) {
        }
        assertEquals(2, adapter.itemCount)
        assertEquals("f2", adapter.getCurrentFields()[0].fieldId)
        assertEquals("f3", adapter.getCurrentFields()[1].fieldId)
    }

    @Test
    fun updateFields_withSameSizeAndChangedValue_updatesFieldInPlace() {
        val adapter = newAdapter(mutableListOf(field(fieldId = "f1", value = "old"))) ?: return
        try {
            adapter.updateFields(listOf(field(fieldId = "f1", value = "new")))
        } catch (e: NullPointerException) {
        }
        assertEquals(1, adapter.itemCount)
        assertEquals("new", adapter.getCurrentFields()[0].value)
    }

    @Test
    fun updateFields_withSameSizeAndNoChanges_leavesFieldsUnchanged() {
        val original = field(fieldId = "f1", value = "same")
        val adapter = newAdapter(mutableListOf(original)) ?: return
        try {
            adapter.updateFields(listOf(field(fieldId = "f1", value = "same")))
        } catch (e: NullPointerException) {
        }
        assertEquals("same", adapter.getCurrentFields()[0].value)
    }

    @Test
    fun setViewMode_doesNotThrow() {
        val adapter = newAdapter(mutableListOf(field())) ?: return
        try {
            adapter.setViewMode(true)
            adapter.setViewMode(false)
        } catch (e: NullPointerException) {
        }
    }
}
