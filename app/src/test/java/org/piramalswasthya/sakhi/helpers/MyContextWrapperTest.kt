package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Locale

class MyContextWrapperTest {

    private fun contextWithResources(): Pair<Context, Resources> {
        val resources: Resources = mockk(relaxed = true)
        val configuration: Configuration = mockk(relaxed = true)
        every { resources.configuration } returns configuration
        every { resources.displayMetrics } returns mockk(relaxed = true)
        val context: Context = mockk(relaxed = true)
        every { context.resources } returns resources
        return context to resources
    }

    @Test
    fun `updateResourcesLocaleLegacy applies configuration and returns context`() {
        val (context, resources) = contextWithResources()
        val result = MyContextWrapper.updateResourcesLocaleLegacy(context, Locale("hi"))
        assertNotNull(result)
        verify { resources.updateConfiguration(any(), any()) }
    }

    @Test
    fun `updateResourcesLocale creates configuration context`() {
        val (context, _) = contextWithResources()
        val created: Context = mockk(relaxed = true)
        every { context.createConfigurationContext(any()) } returns created
        val result = MyContextWrapper.updateResourcesLocale(context, Locale("mr"))
        assertNotNull(result)
        verify { context.createConfigurationContext(any()) }
    }

    @Test
    fun `updateBaseContextLocale sets default locale and returns non-null`() {
        val (context, _) = contextWithResources()
        every { context.createConfigurationContext(any()) } returns mockk(relaxed = true)
        val result = MyContextWrapper.updateBaseContextLocale("bn", context)
        assertNotNull(result)
        // Locale.setDefault should have been applied
        org.junit.Assert.assertEquals("bn", Locale.getDefault().language)
    }
}
