package org.piramalswasthya.sakhi.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NavigationExtTest {

    private val actionId = 101
    private val sourceId = 1
    private val targetId = 2

    private lateinit var navController: NavController
    private lateinit var source: NavDestination
    private lateinit var target: NavDestination
    private lateinit var directions: NavDirections
    private lateinit var fragment: Fragment

    @Before
    fun setUp() {
        navController = mockk(relaxed = true)
        source = mockk(relaxed = true)
        target = mockk(relaxed = true)
        directions = mockk(relaxed = true)
        fragment = mockk(relaxed = true)

        every { source.id } returns sourceId
        every { target.id } returns targetId
        every { source.getAction(actionId) } returns NavAction(targetId)
        every { target.getAction(actionId) } returns null
        every { directions.actionId } returns actionId
        every { navController.currentDestination } returns source

        mockkStatic(Fragment::findNavController)
        every { fragment.findNavController() } returns navController
        every { fragment.lifecycle.currentState } returns Lifecycle.State.RESUMED
    }

    @After
    fun tearDown() {
        unmockkStatic(Fragment::findNavController)
    }

    @Test
    fun `navController safeNavigate navigates when action is reachable`() {
        navController.safeNavigate(directions)
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `navController safeNavigate skips when action is not reachable from current destination`() {
        every { navController.currentDestination } returns target
        navController.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `navController safeNavigate skips when there is no current destination`() {
        every { navController.currentDestination } returns null
        navController.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `navController safeNavigate swallows exception thrown by navigate`() {
        every { navController.navigate(directions) } throws IllegalArgumentException("not found")
        navController.safeNavigate(directions)
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `double tap navigates only once after destination has moved on`() {
        every { navController.currentDestination } returnsMany listOf(source, target)
        navController.safeNavigate(directions)
        navController.safeNavigate(directions)
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `repeated state emission navigates only once`() {
        every { navController.currentDestination } returnsMany listOf(source, target, target)
        repeat(3) { navController.safeNavigate(directions) }
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `navController safeNavigate by id navigates when not already at destination`() {
        val args = mockk<Bundle>()
        navController.safeNavigate(targetId, args)
        verify(exactly = 1) { navController.navigate(targetId, args) }
    }

    @Test
    fun `navController safeNavigate by id skips when already at destination`() {
        every { navController.currentDestination } returns target
        navController.safeNavigate(targetId)
        verify(exactly = 0) { navController.navigate(any<Int>(), any<Bundle>()) }
    }

    @Test
    fun `navController safeNavigate by id skips when there is no current destination`() {
        every { navController.currentDestination } returns null
        navController.safeNavigate(targetId)
        verify(exactly = 0) { navController.navigate(any<Int>(), any<Bundle>()) }
    }

    @Test
    fun `navController safeNavigate by id swallows exception thrown by navigate`() {
        every { navController.navigate(targetId, null) } throws IllegalArgumentException("not found")
        navController.safeNavigate(targetId)
        verify(exactly = 1) { navController.navigate(targetId, null) }
    }

    @Test
    fun `fragment safeNavigate navigates when resumed and action reachable`() {
        fragment.safeNavigate(directions)
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `fragment safeNavigate skips when fragment is only started`() {
        every { fragment.lifecycle.currentState } returns Lifecycle.State.STARTED
        fragment.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `fragment safeNavigate skips when fragment is destroyed`() {
        every { fragment.lifecycle.currentState } returns Lifecycle.State.DESTROYED
        fragment.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `fragment safeNavigate skips when fragment has no nav controller`() {
        every { fragment.findNavController() } throws IllegalStateException("no nav controller")
        fragment.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `fragment safeNavigate skips stale click when action no longer reachable`() {
        every { navController.currentDestination } returns target
        fragment.safeNavigate(directions)
        verify(exactly = 0) { navController.navigate(any<NavDirections>()) }
    }

    @Test
    fun `fragment double tap navigates only once`() {
        every { navController.currentDestination } returnsMany listOf(source, target)
        fragment.safeNavigate(directions)
        fragment.safeNavigate(directions)
        verify(exactly = 1) { navController.navigate(directions) }
    }

    @Test
    fun `fragment safeNavigate by id navigates when resumed`() {
        fragment.safeNavigate(targetId)
        verify(exactly = 1) { navController.navigate(targetId, null) }
    }

    @Test
    fun `fragment safeNavigate by id skips when not resumed`() {
        every { fragment.lifecycle.currentState } returns Lifecycle.State.CREATED
        fragment.safeNavigate(targetId)
        verify(exactly = 0) { navController.navigate(any<Int>(), any<Bundle>()) }
    }
}
