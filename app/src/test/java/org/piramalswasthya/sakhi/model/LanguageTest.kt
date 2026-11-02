package org.piramalswasthya.sakhi.model

import android.graphics.drawable.Drawable
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.helpers.Languages

class LanguageTest {

    @Test
    fun `constructor uses default isSelected when omitted`() {
        val language = Language(
            id = 1,
            lanFirstWord = "अ",
            lanName = "Hindi",
            lanSelectedView = mockk<Drawable>(relaxed = true),
            lanUnselectedView = mockk<Drawable>(relaxed = true),
            language = Languages.HINDI
        )

        assertNotNull(language)
        assertFalse(language.isSelected)
    }
}
