package org.piramalswasthya.sakhi.adapters

import android.graphics.drawable.Drawable
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.Language

class LanguageAdapterTest {

    private fun language(id: Int = 1, isSelected: Boolean = false) = Language(
        id = id,
        lanFirstWord = "A",
        lanName = "English",
        lanSelectedView = mockk<Drawable>(relaxed = true),
        lanUnselectedView = mockk<Drawable>(relaxed = true),
        language = Languages.ENGLISH,
        isSelected = isSelected
    )

    @Test
    fun itemCount_matchesListSize() {
        val adapter = LanguageAdapter(listOf(language(1), language(2)), onItemClick = {})
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = LanguageAdapter(emptyList(), onItemClick = {})
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun onItemClick_lambdaIsInvokedWithCorrectLanguage() {
        var captured: Language? = null
        val target = language(id = 5)
        val adapter = LanguageAdapter(listOf(target)) { captured = it }
        val field = LanguageAdapter::class.java.getDeclaredField("onItemClick")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val lambda = field.get(adapter) as (Language) -> Unit
        lambda(target)
        assertEquals(target, captured)
    }
}
