package org.piramalswasthya.sakhi.configuration

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.model.SurveyRegisterCache

class SurveyRegisterFormDatasetTest {

    private val context: Context = mockk(relaxed = true)

    @Test
    fun dataset_buildsEveryFormInput_withoutASavedRecord() {
        assertNotNull(SurveyRegisterFormDataset(context))
    }

    @Test
    fun dataset_buildsEveryFormInput_withASavedRecord() {
        assertNotNull(SurveyRegisterFormDataset(context, mockk<SurveyRegisterCache>(relaxed = true)))
    }

    @Test
    fun dataset_buildsEveryFormInput_withAnExplicitNullRecord() {
        assertNotNull(SurveyRegisterFormDataset(context, null))
    }

    @Test
    fun dataset_doesNotTouchTheContext() {
        val strictContext = mockk<Context>()
        assertNotNull(SurveyRegisterFormDataset(strictContext))
    }
}
