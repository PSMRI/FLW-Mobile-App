package org.piramalswasthya.sakhi.helpers

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo

class RecapStoryGateTest {

    private val context = mockk<Context>()
    private val repo = mockk<MonthlyRecapRepo>()
    private val library = mockk<RecapContentLibrary>()
    private val scene = RecapScene(RecapScene.Type.WELCOME, "hello", lottieRawRes = null)

    @Before
    fun setUp() = mockkObject(RecapContentCodec)

    @After
    fun tearDown() = unmockkObject(RecapContentCodec)

    @Test
    fun `composes scenes and parses the bundled library once`() = runTest {
        every { RecapContentCodec.loadBundled(context) } returns library
        coEvery { repo.buildPersonalizedScenes(library) } returns listOf(scene)
        val gate = RecapStoryGate(context, repo)

        assertEquals(listOf(scene), gate.composeStory())
        assertTrue(gate.hasStory())
        verify(exactly = 1) { RecapContentCodec.loadBundled(context) }
    }

    @Test
    fun `empty month or missing user gives no story`() = runTest {
        every { RecapContentCodec.loadBundled(context) } returns library
        coEvery { repo.buildPersonalizedScenes(library) } returns null
        val gate = RecapStoryGate(context, repo)

        assertEquals(emptyList<RecapScene>(), gate.composeStory())
        assertFalse(gate.hasStory())
    }

    @Test
    fun `unusable content library keeps the recap closed`() = runTest {
        every { RecapContentCodec.loadBundled(context) } returns null
        val gate = RecapStoryGate(context, repo)

        assertFalse(gate.hasStory())
    }
}
