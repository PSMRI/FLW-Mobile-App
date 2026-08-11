package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo

@OptIn(ExperimentalCoroutinesApi::class)
class SaasBahuSammelanPullWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val saasBahuSammelanRepo: SaasBahuSammelanRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): SaasBahuSammelanPullWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return SaasBahuSammelanPullWorker(context, params, saasBahuSammelanRepo)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }
}
