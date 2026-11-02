package org.piramalswasthya.sakhi.adapters

import android.net.Uri
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class FileListAdapterTest {

    @Test
    fun itemCount_reflectsInitialListSize() {
        val images = mutableListOf(mockk<Uri>(), mockk<Uri>())
        val adapter = FileListAdapter(images)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = FileListAdapter(mutableListOf())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun updateFileList_replacesContentsAndUpdatesCount() {
        val adapter = FileListAdapter(mutableListOf(mockk<Uri>()))
        val newList = mutableListOf(mockk<Uri>(), mockk<Uri>(), mockk<Uri>())
        try {
            adapter.updateFileList(newList)
        } catch (e: NullPointerException) {
        }
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun updateFileList_withEmptyList_clearsCount() {
        val adapter = FileListAdapter(mutableListOf(mockk<Uri>(), mockk<Uri>()))
        try {
            adapter.updateFileList(mutableListOf())
        } catch (e: NullPointerException) {
        }
        assertEquals(0, adapter.itemCount)
    }
}
