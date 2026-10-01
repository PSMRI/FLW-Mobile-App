package org.piramalswasthya.sakhi.adapters

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.PreviewItem

class PreviewAdapterTest {

    private fun viewHolder(): Triple<PreviewAdapter.PreviewVH, TextView, ImageView> {
        val tvLabel = mockk<TextView>(relaxed = true)
        val tvValue = mockk<TextView>(relaxed = true)
        val ivImage = mockk<ImageView>(relaxed = true)
        val view = mockk<View>(relaxed = true)
        every { view.findViewById<TextView>(R.id.tvLabel) } returns tvLabel
        every { view.findViewById<TextView>(R.id.tvValue) } returns tvValue
        every { view.findViewById<ImageView>(R.id.ivPreviewImage) } returns ivImage
        return Triple(PreviewAdapter.PreviewVH(view), tvValue, ivImage)
    }

    @Test
    fun getItemCount_returnsItemsSize() {
        val items = listOf(
            PreviewItem(label = "L1", value = "V1"),
            PreviewItem(label = "L2", value = "V2")
        )
        val adapter = PreviewAdapter(items)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun getItemCount_isZeroForEmptyList() {
        val adapter = PreviewAdapter(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun onBindViewHolder_textItem_setsTextValue() {
        val (holder, tvValue, ivImage) = viewHolder()
        val items = listOf(PreviewItem(label = "Label", value = "Value", isImage = false))
        val adapter = PreviewAdapter(items)
        adapter.onBindViewHolder(holder, 0)
        verify { tvValue.text = "Value" }
        verify { tvValue.visibility = View.VISIBLE }
        verify { ivImage.visibility = View.GONE }
    }

    @Test
    fun onBindViewHolder_imageItem_invokesInjectedImageLoader() {
        val (holder, _, ivImage) = viewHolder()
        val uri = mockk<Uri>()
        val items = listOf(PreviewItem(label = "Label", value = "", isImage = true, imageUri = uri))
        var capturedView: ImageView? = null
        var capturedUri: Uri? = null
        val adapter = PreviewAdapter(items) { iv, u ->
            capturedView = iv
            capturedUri = u
        }
        adapter.onBindViewHolder(holder, 0)
        assertEquals(ivImage, capturedView)
        assertEquals(uri, capturedUri)
    }

    @Test
    fun onBindViewHolder_imageItemWithoutLoader_doesNotThrow() {
        val (holder, _, _) = viewHolder()
        val items = listOf(PreviewItem(label = "Label", value = "", isImage = true, imageUri = null))
        val adapter = PreviewAdapter(items)
        adapter.onBindViewHolder(holder, 0)
    }
}
