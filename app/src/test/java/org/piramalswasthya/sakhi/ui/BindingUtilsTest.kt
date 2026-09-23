package org.piramalswasthya.sakhi.ui

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AncFormState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.VaccineState
import java.io.ByteArrayInputStream
import java.util.Calendar

class BindingUtilsTest {

    @Before
    fun setUp() {
        mockkConstructor(RotateAnimation::class, recordPrivateCalls = false)
        every { anyConstructed<RotateAnimation>().duration = any() } just Runs
        every { anyConstructed<RotateAnimation>().interpolator = any() } just Runs
        every { anyConstructed<RotateAnimation>().repeatCount = any() } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun contextWithArray(arrayResId: Int, values: Array<String>): Context {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getStringArray(arrayResId) } returns values
        return ctx
    }

    // =====================================================
    // IconDataset.Disease.getTitleRes() / getTitle()
    // =====================================================

    @Test
    fun `getTitleRes maps every disease to its string resource`() {
        assertEquals(R.string.icon_title_maleria, IconDataset.Disease.MALARIA.getTitleRes())
        assertEquals(R.string.icon_title_ka, IconDataset.Disease.KALA_AZAR.getTitleRes())
        assertEquals(R.string.icon_title_aes, IconDataset.Disease.AES_JE.getTitleRes())
        assertEquals(R.string.icon_title_filaria, IconDataset.Disease.FILARIA.getTitleRes())
        assertEquals(R.string.icon_title_leprosy, IconDataset.Disease.LEPROSY.getTitleRes())
        assertEquals(R.string.deworming_title, IconDataset.Disease.DEWARMING.getTitleRes())
    }

    @Test
    fun `getTitle resolves the string via the context`() {
        val context = mockk<Context>(relaxed = true)
        every { context.getString(R.string.icon_title_maleria) } returns "Malaria"

        val result = IconDataset.Disease.MALARIA.getTitle(context)

        assertEquals("Malaria", result)
        verify { context.getString(R.string.icon_title_maleria) }
    }

    // =====================================================
    // ImageView.setVaccineState()
    // =====================================================

    @Test
    fun `ImageView setVaccineState DONE sets the check circle drawable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(VaccineState.DONE)
        verify { view.setImageResource(R.drawable.ic_check_circle_green) }
    }

    @Test
    fun `ImageView setVaccineState MISSED sets the crossed circle drawable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(VaccineState.MISSED)
        verify { view.setImageResource(R.drawable.ic_crossed_circle) }
    }

    @Test
    fun `ImageView setVaccineState PENDING sets the add circle drawable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(VaccineState.PENDING)
        verify { view.setImageResource(R.drawable.ic_add_circle) }
    }

    @Test
    fun `ImageView setVaccineState OVERDUE sets the event available drawable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(VaccineState.OVERDUE)
        verify { view.setImageResource(R.drawable.ic_event_available) }
    }

    @Test
    fun `ImageView setVaccineState UNAVAILABLE sets no drawable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(VaccineState.UNAVAILABLE)
        verify(exactly = 0) { view.setImageResource(any()) }
    }

    @Test
    fun `ImageView setVaccineState null does nothing`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setVaccineState(null)
        verify(exactly = 0) { view.setImageResource(any()) }
    }

    // =====================================================
    // Button.setVaccineState()
    // =====================================================

    private fun buttonWithVaccineStrings(): Button {
        val button = mockk<Button>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { button.resources } returns resources
        every { resources.getString(R.string.vaccine_fill) } returns "Fill"
        every { resources.getString(R.string.view) } returns "View"
        return button
    }

    @Test
    fun `Button setVaccineState PENDING shows the fill label`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(VaccineState.PENDING)
        verify { button.visibility = View.VISIBLE }
        verify { button.text = "Fill" }
    }

    @Test
    fun `Button setVaccineState OVERDUE shows the fill label`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(VaccineState.OVERDUE)
        verify { button.text = "Fill" }
    }

    @Test
    fun `Button setVaccineState DONE shows the view label`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(VaccineState.DONE)
        verify { button.text = "View" }
    }

    @Test
    fun `Button setVaccineState MISSED hides the button`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(VaccineState.MISSED)
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `Button setVaccineState UNAVAILABLE hides the button`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(VaccineState.UNAVAILABLE)
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `Button setVaccineState null does nothing`() {
        val button = buttonWithVaccineStrings()
        button.setVaccineState(null)
        verify(exactly = 0) { button.visibility = any() }
    }

    // =====================================================
    // setFormattedDate() / setFormattedDateWithMonth()
    // =====================================================

    private fun march17_2026(): Long =
        Calendar.getInstance().apply { set(2026, Calendar.MARCH, 17, 0, 0, 0) }.timeInMillis

    @Test
    fun `setFormattedDate formats a timestamp as dd-MM-yyyy`() {
        val view = mockk<TextView>(relaxed = true)
        setFormattedDate(view, march17_2026())
        verify { view.text = "17-03-2026" }
    }

    @Test
    fun `setFormattedDate does nothing for a null timestamp`() {
        val view = mockk<TextView>(relaxed = true)
        setFormattedDate(view, null)
        verify(exactly = 0) { view.text = any() }
    }

    @Test
    fun `setFormattedDateWithMonth formats a timestamp with the month name`() {
        val view = mockk<TextView>(relaxed = true)
        setFormattedDateWithMonth(view, march17_2026())
        verify { view.text = "17-03-2026 , Mar" }
    }

    @Test
    fun `setFormattedDateWithMonth does nothing for a null timestamp`() {
        val view = mockk<TextView>(relaxed = true)
        setFormattedDateWithMonth(view, null)
        verify(exactly = 0) { view.text = any() }
    }

    // =====================================================
    // TextView.setRecordCount()
    // =====================================================

    @Test
    fun `setRecordCount collects the flow and sets the count text`() = runTest {
        val view = mockk<TextView>(relaxed = true)
        view.setRecordCount(this, flowOf(5))
        advanceUntilIdle()
        verify { view.text = "5" }
    }

    @Test
    fun `setRecordCount clears the text for a null flow`() = runTest {
        val view = mockk<TextView>(relaxed = true)
        view.setRecordCount(this, null)
        verify { view.text = null }
    }

    // =====================================================
    // CardView.setRedBorder()
    // =====================================================

    @Test
    fun `setRedBorder shows the red border when count is positive and allowed`() = runTest {
        val cardView = mockk<CardView>(relaxed = true)
        cardView.setRedBorder(true, this, flowOf(3))
        advanceUntilIdle()
        verify { cardView.setBackgroundResource(R.drawable.red_border) }
    }

    @Test
    fun `setRedBorder does nothing when not allowed`() = runTest {
        val cardView = mockk<CardView>(relaxed = true)
        cardView.setRedBorder(false, this, flowOf(3))
        advanceUntilIdle()
        verify(exactly = 0) { cardView.setBackgroundResource(any()) }
    }

    @Test
    fun `setRedBorder does nothing when count is zero`() = runTest {
        val cardView = mockk<CardView>(relaxed = true)
        cardView.setRedBorder(true, this, flowOf(0))
        advanceUntilIdle()
        verify(exactly = 0) { cardView.setBackgroundResource(any()) }
    }

    @Test
    fun `setRedBorder does nothing for a null flow`() = runTest {
        val cardView = mockk<CardView>(relaxed = true)
        cardView.setRedBorder(true, this, null)
        advanceUntilIdle()
        verify(exactly = 0) { cardView.setBackgroundResource(any()) }
    }

    // =====================================================
    // TextView.setBenIdText()
    // =====================================================

    @Test
    fun `setBenIdText shows pending sync for a negative id`() {
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getColor(android.R.color.holo_orange_light) } returns 111

        view.setBenIdText(-5L)

        verify { view.text = "Pending Sync" }
        verify { view.setTextColor(111) }
    }

    @Test
    fun `setBenIdText shows the id for a non negative value`() {
        mockkStatic(MaterialColors::class)
        val view = mockk<TextView>(relaxed = true)
        every { MaterialColors.getColor(any<View>(), any<Int>()) } returns 222

        view.setBenIdText(42L)

        verify { view.text = "42" }
        verify { view.setTextColor(222) }
    }

    @Test
    fun `setBenIdText does nothing for a null id`() {
        val view = mockk<TextView>(relaxed = true)
        view.setBenIdText(null)
        verify(exactly = 0) { view.text = any() }
    }

    // =====================================================
    // TextView.showBasedOnNumMembers()
    // =====================================================

    @Test
    fun `showBasedOnNumMembers shows the view for a positive count`() {
        val view = mockk<TextView>(relaxed = true)
        view.showBasedOnNumMembers(3)
        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `showBasedOnNumMembers hides the view for zero`() {
        val view = mockk<TextView>(relaxed = true)
        view.showBasedOnNumMembers(0)
        verify { view.visibility = View.GONE }
    }

    @Test
    fun `showBasedOnNumMembers does nothing for null`() {
        val view = mockk<TextView>(relaxed = true)
        view.showBasedOnNumMembers(null)
        verify(exactly = 0) { view.visibility = any() }
    }

    // =====================================================
    // CardView.setBackgroundTintBasedOnNumMembers()
    // =====================================================

    @Test
    fun `setBackgroundTintBasedOnNumMembers tints for a positive count`() {
        mockkStatic(MaterialColors::class)
        val cardView = mockk<CardView>(relaxed = true)
        every { MaterialColors.getColor(any<View>(), any<Int>()) } returns 333

        cardView.setBackgroundTintBasedOnNumMembers(2)

        verify { cardView.setCardBackgroundColor(333) }
    }

    @Test
    fun `setBackgroundTintBasedOnNumMembers tints for zero`() {
        mockkStatic(MaterialColors::class)
        val cardView = mockk<CardView>(relaxed = true)
        every { MaterialColors.getColor(any<View>(), any<Int>()) } returns 444

        cardView.setBackgroundTintBasedOnNumMembers(0)

        verify { cardView.setCardBackgroundColor(444) }
    }

    @Test
    fun `setBackgroundTintBasedOnNumMembers does nothing for null`() {
        val cardView = mockk<CardView>(relaxed = true)
        cardView.setBackgroundTintBasedOnNumMembers(null)
        verify(exactly = 0) { cardView.setCardBackgroundColor(any<Int>()) }
    }

    // =====================================================
    // LinearLayout.showRchIdOrNot()
    // =====================================================

    private fun benWithGenderAndAge(gender: String, ageInt: Int): BenBasicDomain {
        val ben = mockk<BenBasicDomain>(relaxed = true)
        every { ben.gender } returns gender
        every { ben.ageInt } returns ageInt
        return ben
    }

    @Test
    fun `showRchIdOrNot shows the rch id for a female`() {
        val view = mockk<LinearLayout>(relaxed = true)
        view.showRchIdOrNot(benWithGenderAndAge(Gender.FEMALE.name, 30))
        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `showRchIdOrNot shows the rch id for a young male`() {
        val view = mockk<LinearLayout>(relaxed = true)
        view.showRchIdOrNot(benWithGenderAndAge(Gender.MALE.name, 10))
        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `showRchIdOrNot hides the rch id for an older male`() {
        val view = mockk<LinearLayout>(relaxed = true)
        view.showRchIdOrNot(benWithGenderAndAge(Gender.MALE.name, 40))
        verify { view.visibility = View.INVISIBLE }
    }

    @Test
    fun `showRchIdOrNot does nothing for a null ben`() {
        val view = mockk<LinearLayout>(relaxed = true)
        view.showRchIdOrNot(null)
        verify(exactly = 0) { view.visibility = any() }
    }

    // =====================================================
    // TextView.textBasedOnNumMembers()
    // =====================================================

    @Test
    fun `textBasedOnNumMembers shows add member text for a positive count`() {
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.str_add_member) } returns "Add Member"

        view.textBasedOnNumMembers(2)

        verify { view.text = "Add Member" }
    }

    @Test
    fun `textBasedOnNumMembers shows add family member text for zero`() {
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.add_family_member) } returns "Add Family Member"

        view.textBasedOnNumMembers(0)

        verify { view.text = "Add Family Member" }
    }

    @Test
    fun `textBasedOnNumMembers does nothing for null`() {
        val view = mockk<TextView>(relaxed = true)
        view.textBasedOnNumMembers(null)
        verify(exactly = 0) { view.text = any() }
    }

    // =====================================================
    // AutoCompleteTextView.setSpinnerItems()
    // =====================================================

    @Test
    fun `setSpinnerItems sets an adapter for a non null list`() {
        mockkConstructor(ArrayAdapter::class)
        val view = mockk<AutoCompleteTextView>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context

        view.setSpinnerItems(arrayOf("A", "B"))

        verify { view.setAdapter(any<ArrayAdapter<String>>()) }
    }

    @Test
    fun `setSpinnerItems does nothing for a null list`() {
        val view = mockk<AutoCompleteTextView>(relaxed = true)
        view.setSpinnerItems(null)
        verify(exactly = 0) { view.setAdapter(any<ArrayAdapter<String>>()) }
    }

    // =====================================================
    // TextInputEditText.setAllAlphabetCaps()
    // =====================================================

    @Test
    fun `setAllAlphabetCaps enables caps and the caps input type`() {
        val view = mockk<TextInputEditText>(relaxed = true)
        view.setAllAlphabetCaps(true)
        verify { view.setAllCaps(true) }
        verify { view.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) }
    }

    @Test
    fun `setAllAlphabetCaps does nothing when false`() {
        val view = mockk<TextInputEditText>(relaxed = true)
        view.setAllAlphabetCaps(false)
        verify(exactly = 0) { view.setAllCaps(any()) }
    }

    // =====================================================
    // {Button,ImageView,ViewGroup}.setVisibilityOfLayout()
    // =====================================================

    @Test
    fun `Button setVisibilityOfLayout shows and hides and ignores null`() {
        val shown = mockk<Button>(relaxed = true)
        shown.setVisibilityOfLayout(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<Button>(relaxed = true)
        hidden.setVisibilityOfLayout(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<Button>(relaxed = true)
        untouched.setVisibilityOfLayout(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    @Test
    fun `ImageView setVisibilityOfLayout shows and hides and ignores null`() {
        val shown = mockk<ImageView>(relaxed = true)
        shown.setVisibilityOfLayout(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<ImageView>(relaxed = true)
        hidden.setVisibilityOfLayout(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<ImageView>(relaxed = true)
        untouched.setVisibilityOfLayout(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    @Test
    fun `ViewGroup setVisibilityOfLayout shows and hides and ignores null`() {
        val shown = mockk<ViewGroup>(relaxed = true)
        shown.setVisibilityOfLayout(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<ViewGroup>(relaxed = true)
        hidden.setVisibilityOfLayout(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<ViewGroup>(relaxed = true)
        untouched.setVisibilityOfLayout(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    // =====================================================
    // ConstraintLayout.setItems() (radioForm) - no-op body
    // =====================================================

    @Test
    fun `setItems no-op form binder does not throw`() {
        val layout = mockk<ConstraintLayout>(relaxed = true)
        layout.setItems(null)
        layout.setItems(mockk(relaxed = true))
    }

    // =====================================================
    // ConstraintLayout.setItemsCheckBox()
    // =====================================================

    @Test
    fun `setItemsCheckBox clears existing views when the form is null`() {
        val layout = mockk<ConstraintLayout>(relaxed = true)
        val ll = mockk<LinearLayout>(relaxed = true)
        every { layout.findViewById<LinearLayout>(R.id.ll_checks) } returns ll

        layout.setItemsCheckBox(null)

        verify { ll.removeAllViews() }
    }

    @Test
    fun `setItemsCheckBox builds a checkbox per entry and toggles the form value`() {
        mockkStatic(View::class)
        every { View.generateViewId() } returns 101
        mockkConstructor(CheckBox::class)
        every { anyConstructed<CheckBox>().layoutParams = any() } just Runs
        every { anyConstructed<CheckBox>().id = any() } just Runs
        every { anyConstructed<CheckBox>().setTextAppearance(any(), any()) } just Runs
        every { anyConstructed<CheckBox>().setTextAppearance(any()) } just Runs
        every { anyConstructed<CheckBox>().text = any() } just Runs
        every { anyConstructed<CheckBox>().isChecked = any() } just Runs
        every { anyConstructed<CheckBox>().setOnCheckedChangeListener(any()) } just Runs
        val listenerSlot = slot<CompoundButton.OnCheckedChangeListener>()
        every { anyConstructed<CheckBox>().setOnCheckedChangeListener(capture(listenerSlot)) } just Runs

        val layout = mockk<ConstraintLayout>(relaxed = true)
        val ll = mockk<LinearLayout>(relaxed = true)
        val llContext = mockk<Context>(relaxed = true)
        every { layout.findViewById<LinearLayout>(R.id.ll_checks) } returns ll
        every { ll.context } returns llContext

        val form = FormInputOld(
            inputType = InputType.CHECKBOXES,
            title = "Symptoms",
            entries = arrayOf("Fever"),
            required = true
        )

        layout.setItemsCheckBox(form)

        verify { ll.addView(any()) }

        val checkedChangeListener = listenerSlot.captured
        checkedChangeListener.onCheckedChanged(mockk(relaxed = true), true)
        assertEquals("Fever", form.value.value)

        checkedChangeListener.onCheckedChanged(mockk(relaxed = true), false)
        assertNull(form.value.value)

        verify { layout.setBackgroundResource(0) }
    }

    @Test
    fun `setItemsCheckBox pre-checks an entry already present in the form value`() {
        mockkStatic(View::class)
        every { View.generateViewId() } returns 202
        mockkConstructor(CheckBox::class)
        every { anyConstructed<CheckBox>().layoutParams = any() } just Runs
        every { anyConstructed<CheckBox>().id = any() } just Runs
        every { anyConstructed<CheckBox>().setTextAppearance(any(), any()) } just Runs
        every { anyConstructed<CheckBox>().setTextAppearance(any()) } just Runs
        every { anyConstructed<CheckBox>().text = any() } just Runs
        every { anyConstructed<CheckBox>().isChecked = any() } just Runs
        every { anyConstructed<CheckBox>().setOnCheckedChangeListener(any()) } just Runs

        val layout = mockk<ConstraintLayout>(relaxed = true)
        val ll = mockk<LinearLayout>(relaxed = true)
        val llContext = mockk<Context>(relaxed = true)
        every { layout.findViewById<LinearLayout>(R.id.ll_checks) } returns ll
        every { ll.context } returns llContext

        val form = FormInputOld(
            inputType = InputType.CHECKBOXES,
            title = "Symptoms",
            entries = arrayOf("Cough"),
            required = true
        )
        form.value.value = "Cough"

        layout.setItemsCheckBox(form)

        verify { anyConstructed<CheckBox>().isChecked = true }
    }

    // =====================================================
    // TextView.setRequired() / ImageView.setRequired() / TextView.setRequired2()
    // =====================================================

    @Test
    fun `TextView setRequired shows and hides and ignores null`() {
        val shown = mockk<TextView>(relaxed = true)
        shown.setRequired(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<TextView>(relaxed = true)
        hidden.setRequired(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<TextView>(relaxed = true)
        untouched.setRequired(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    @Test
    fun `ImageView setRequired shows and hides and ignores null`() {
        val shown = mockk<ImageView>(relaxed = true)
        shown.setRequired(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<ImageView>(relaxed = true)
        hidden.setRequired(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<ImageView>(relaxed = true)
        untouched.setRequired(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    @Test
    fun `TextView setRequired2 shows and hides and ignores null`() {
        val shown = mockk<TextView>(relaxed = true)
        shown.setRequired2(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<TextView>(relaxed = true)
        hidden.setRequired2(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<TextView>(relaxed = true)
        untouched.setRequired2(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    // =====================================================
    // MaterialDivider.setHeadingLine()
    // =====================================================

    @Test
    fun `setHeadingLine shows and hides and ignores null`() {
        val shown = mockk<MaterialDivider>(relaxed = true)
        shown.setHeadingLine(true)
        verify { shown.visibility = View.VISIBLE }

        val hidden = mockk<MaterialDivider>(relaxed = true)
        hidden.setHeadingLine(false)
        verify { hidden.visibility = View.GONE }

        val untouched = mockk<MaterialDivider>(relaxed = true)
        untouched.setHeadingLine(null)
        verify(exactly = 0) { untouched.visibility = any() }
    }

    // =====================================================
    // ImageView.setSyncState() / setSyncStateForBen()
    // =====================================================

    @Test
    fun `setSyncState shows unsynced and is clickable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncState(SyncState.UNSYNCED)
        verify { view.setImageResource(R.drawable.ic_unsynced) }
        verify { view.isClickable = true }
        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `setSyncState shows syncing and starts the rotate animation`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncState(SyncState.SYNCING)
        verify { view.setImageResource(R.drawable.ic_syncing) }
        verify { view.isClickable = false }
        verify { view.startAnimation(any()) }
    }

    @Test
    fun `setSyncState shows synced and is not clickable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncState(SyncState.SYNCED)
        verify { view.setImageResource(R.drawable.ic_synced) }
        verify { view.isClickable = false }
    }

    @Test
    fun `setSyncState hides the view for a null state`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncState(null)
        verify { view.visibility = View.INVISIBLE }
    }

    @Test
    fun `setSyncStateForBen shows unsynced and is clickable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncStateForBen(SyncState.UNSYNCED)
        verify { view.setImageResource(R.drawable.ic_unsynced) }
        verify { view.isClickable = true }
    }

    @Test
    fun `setSyncStateForBen shows syncing and starts the rotate animation`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncStateForBen(SyncState.SYNCING)
        verify { view.setImageResource(R.drawable.ic_syncing) }
        verify { view.isClickable = false }
        verify { view.startAnimation(any()) }
    }

    @Test
    fun `setSyncStateForBen shows synced and is not clickable`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncStateForBen(SyncState.SYNCED)
        verify { view.setImageResource(R.drawable.ic_synced) }
        verify { view.isClickable = false }
    }

    @Test
    fun `setSyncStateForBen hides the view for a null state`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setSyncStateForBen(null)
        verify { view.visibility = View.INVISIBLE }
    }

    // =====================================================
    // ImageView.setBenImage()
    // =====================================================

    @Test
    fun `setBenImage sets the person placeholder for a null uri`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setBenImage(null)
        verify { view.setImageResource(R.drawable.ic_person) }
    }

    // =====================================================
    // Button.setCbacListAvail()
    // =====================================================

    @Test
    fun `setCbacListAvail hides the button for an empty list`() {
        val button = mockk<Button>(relaxed = true)
        button.setCbacListAvail(emptyList<Any>())
        verify { button.visibility = View.INVISIBLE }
    }

    @Test
    fun `setCbacListAvail shows the button for a non empty list`() {
        val button = mockk<Button>(relaxed = true)
        button.setCbacListAvail(listOf("x"))
        verify { button.visibility = View.VISIBLE }
    }

    @Test
    fun `setCbacListAvail does nothing for a null list`() {
        val button = mockk<Button>(relaxed = true)
        button.setCbacListAvail(null)
        verify(exactly = 0) { button.visibility = any() }
    }

    // =====================================================
    // ImageView.setAncState()
    // =====================================================

    @Test
    fun `setAncState maps every anc form state to its drawable`() {
        val allowFill = mockk<ImageView>(relaxed = true)
        allowFill.setAncState(AncFormState.ALLOW_FILL)
        verify { allowFill.setImageResource(R.drawable.ic_pending_actions) }

        val alreadyFilled = mockk<ImageView>(relaxed = true)
        alreadyFilled.setAncState(AncFormState.ALREADY_FILLED)
        verify { alreadyFilled.setImageResource(R.drawable.ic_check_circle) }

        val noFill = mockk<ImageView>(relaxed = true)
        noFill.setAncState(AncFormState.NO_FILL)
        verify { noFill.setImageResource(R.drawable.ic_close) }
    }

    @Test
    fun `setAncState does nothing for a null state`() {
        val view = mockk<ImageView>(relaxed = true)
        view.setAncState(null)
        verify(exactly = 0) { view.setImageResource(any()) }
    }

    // =====================================================
    // TextView.setAsteriskText() (cbac_name)
    // =====================================================

    @Test
    fun `setAsteriskText renders single asterisk html`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac, "Fever") } returns "template"

        view.setAsteriskText("Fever", 1, null)

        verify { view.text = htmlResult }
    }

    @Test
    fun `setAsteriskText renders double asterisk html`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac_ds, "Fever") } returns "template"

        view.setAsteriskText("Fever", 2, null)

        verify { view.text = htmlResult }
    }

    @Test
    fun `setAsteriskText uses the raw field name for any other count`() {
        val view = mockk<TextView>(relaxed = true)
        view.setAsteriskText("Fever", 3, null)
        verify { view.text = "Fever" }
    }

    @Test
    fun `setAsteriskText does nothing for a null field name`() {
        val view = mockk<TextView>(relaxed = true)
        view.setAsteriskText(null, 1, null)
        verify(exactly = 0) { view.text = any() }
    }

    @Test
    fun `setAsteriskText renders single asterisk html using the alt template when provided`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac_alt, "Fever") } returns "template"

        view.setAsteriskText("Fever", 9, 1)

        verify { view.text = htmlResult }
        verify { resources.getString(R.string.radio_title_cbac_alt, "Fever") }
    }

    @Test
    fun `setAsteriskText renders double asterisk html using the alt template when provided`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac_ds_alt, "Fever") } returns "template"

        view.setAsteriskText("Fever", 9, 2)

        verify { view.text = htmlResult }
        verify { resources.getString(R.string.radio_title_cbac_ds_alt, "Fever") }
    }

    // =====================================================
    // TextInputLayout.setAsteriskFormText()
    // =====================================================

    @Test
    fun `setAsteriskFormText renders html hint when required`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextInputLayout>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac, "Weight") } returns "template"

        view.setAsteriskFormText(true, "Weight")

        verify { view.hint = htmlResult }
    }

    @Test
    fun `setAsteriskFormText keeps the plain title when not required`() {
        val view = mockk<TextInputLayout>(relaxed = true)
        view.setAsteriskFormText(false, "Weight")
        verify { view.hint = "Weight" }
    }

    @Test
    fun `setAsteriskFormText does nothing for a null title`() {
        val view = mockk<TextInputLayout>(relaxed = true)
        view.setAsteriskFormText(true, null)
        verify(exactly = 0) { view.hint = any() }
    }

    @Test
    fun `setAsteriskFormText does nothing for a null required flag`() {
        val view = mockk<TextInputLayout>(relaxed = true)
        view.setAsteriskFormText(null, "Weight")
        verify(exactly = 0) { view.hint = any() }
    }

    // =====================================================
    // TextView.setAsteriskTextView()
    // =====================================================

    @Test
    fun `setAsteriskTextView renders html text when required`() {
        mockkStatic(Html::class)
        val htmlResult = mockk<Spanned>(relaxed = true)
        every { Html.fromHtml(any<String>(), any<Int>()) } returns htmlResult
        val view = mockk<TextView>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { view.resources } returns resources
        every { resources.getString(R.string.radio_title_cbac, "Height") } returns "template"

        view.setAsteriskTextView(true, "Height")

        verify { view.text = htmlResult }
    }

    @Test
    fun `setAsteriskTextView keeps the plain title when not required`() {
        val view = mockk<TextView>(relaxed = true)
        view.setAsteriskTextView(false, "Height")
        verify { view.text = "Height" }
    }

    @Test
    fun `setAsteriskTextView does nothing for a null title`() {
        val view = mockk<TextView>(relaxed = true)
        view.setAsteriskTextView(true, null)
        verify(exactly = 0) { view.text = any() }
    }

    @Test
    fun `setAsteriskTextView does nothing for a null required flag`() {
        val view = mockk<TextView>(relaxed = true)
        view.setAsteriskTextView(null, "Height")
        verify(exactly = 0) { view.text = any() }
    }

    // =====================================================
    // checkFileSize() / getFileSize() / getByteArrayFromUri() / getFileName()
    // =====================================================

    private fun contextWithQueryCursor(cursor: Cursor?): Context {
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.query(any(), any(), any(), any(), any()) } returns cursor
        return ctx
    }

    @Test
    fun `getFileSize reads the size column from the cursor`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(android.provider.OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns 6L * 1024 * 1024
        val ctx = contextWithQueryCursor(cursor)
        val uri = mockk<Uri>(relaxed = true)

        assertEquals(6L * 1024 * 1024, getFileSize(uri, ctx))
        assertTrue(checkFileSize(uri, ctx))
    }

    @Test
    fun `getFileSize returns zero when the cursor is null`() {
        val ctx = contextWithQueryCursor(null)
        val uri = mockk<Uri>(relaxed = true)

        assertEquals(0L, getFileSize(uri, ctx))
        assertFalse(checkFileSize(uri, ctx))
    }

    @Test
    fun `getFileSize returns zero when the size column is missing`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(android.provider.OpenableColumns.SIZE) } returns -1
        val ctx = contextWithQueryCursor(cursor)
        val uri = mockk<Uri>(relaxed = true)

        assertEquals(0L, getFileSize(uri, ctx))
    }

    @Test
    fun `getByteArrayFromUri reads all bytes from the input stream`() {
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<ContentResolver>(relaxed = true)
        val uri = mockk<Uri>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(uri) } returns ByteArrayInputStream(byteArrayOf(1, 2, 3))

        val result = getByteArrayFromUri(uri, ctx)

        assertTrue(result.contentEquals(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `getByteArrayFromUri returns an empty array when the stream is null`() {
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<ContentResolver>(relaxed = true)
        val uri = mockk<Uri>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(uri) } returns null

        val result = getByteArrayFromUri(uri, ctx)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFileName reads the display name from the cursor`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "report.pdf"
        val ctx = contextWithQueryCursor(cursor)
        val uri = mockk<Uri>(relaxed = true)

        assertEquals("report.pdf", getFileName(uri, ctx))
    }

    @Test
    fun `getFileName falls back to a generated name when the cursor has no rows`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.moveToFirst() } returns false
        val ctx = contextWithQueryCursor(cursor)
        val uri = mockk<Uri>(relaxed = true)

        assertTrue(getFileName(uri, ctx).startsWith("file_"))
    }

    // =====================================================
    // setFormattedSessionDate()
    // =====================================================

    @Test
    fun `setFormattedSessionDate shows N-A for a null timestamp`() {
        val view = mockk<TextView>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context
        every { context.getString(R.string.session_date_n_a) } returns "N/A"

        setFormattedSessionDate(view, null)

        verify { view.text = "N/A" }
    }

    @Test
    fun `setFormattedSessionDate formats the default session format`() {
        val view = mockk<TextView>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context
        every { view.tag } returns "default"
        every { context.getString(R.string.session_date_format, "17 Mar 2026") } returns "On 17 Mar 2026"

        setFormattedSessionDate(view, march17_2026())

        verify { view.text = "On 17 Mar 2026" }
    }

    @Test
    fun `setFormattedSessionDate formats the month year session format`() {
        val view = mockk<TextView>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context
        every { view.tag } returns "monthYear"
        every { context.getString(R.string.uwin_session_format, "March - 2026") } returns "Session March - 2026"

        setFormattedSessionDate(view, march17_2026())

        verify { view.text = "Session March - 2026" }
    }

    @Test
    fun `setFormattedSessionDate falls back to the default format for an unrecognized tag`() {
        val view = mockk<TextView>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context
        every { view.tag } returns "somethingElse"
        every { context.getString(R.string.session_date_format, "17 Mar 2026") } returns "On 17 Mar 2026"

        setFormattedSessionDate(view, march17_2026())

        verify { view.text = "On 17 Mar 2026" }
    }

    // =====================================================
    // Button.visibleIfAgeAbove30AndAlive()
    // =====================================================

    @Test
    fun `visibleIfAgeAbove30AndAlive shows for an alive ben above 40`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfAgeAbove30AndAlive(45, null)
        verify { button.visibility = View.VISIBLE }
    }

    @Test
    fun `visibleIfAgeAbove30AndAlive hides for a ben under 40`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfAgeAbove30AndAlive(20, null)
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `visibleIfAgeAbove30AndAlive hides for a dead ben`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfAgeAbove30AndAlive(45, "true")
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `visibleIfAgeAbove30AndAlive treats blank false zero and undefined as alive`() {
        listOf(null, "", "false", "FALSE", "0", "undefined", "UNDEFINED").forEach { deathValue ->
            val button = mockk<Button>(relaxed = true)
            button.visibleIfAgeAbove30AndAlive(41, deathValue)
            verify { button.visibility = View.VISIBLE }
        }
    }

    // =====================================================
    // Button.visibleIfEligibleFemale()
    // =====================================================

    @Test
    fun `visibleIfEligibleFemale shows for an eligible female`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfEligibleFemale(30, null, 1, "female")
        verify { button.visibility = View.VISIBLE }
    }

    @Test
    fun `visibleIfEligibleFemale hides for a male`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfEligibleFemale(30, null, 1, "male")
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `visibleIfEligibleFemale hides for an age outside range`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfEligibleFemale(60, null, 1, "female")
        verify { button.visibility = View.GONE }
    }

    @Test
    fun `visibleIfEligibleFemale hides for an unrelated reproductive status`() {
        val button = mockk<Button>(relaxed = true)
        button.visibleIfEligibleFemale(30, null, 3, "female")
        verify { button.visibility = View.GONE }
    }

    // =====================================================
    // setDynamicBackground()
    // =====================================================

    @Test
    fun `setDynamicBackground sets the error background when eligible`() {
        val view = mockk<View>(relaxed = true)
        setDynamicBackground(view, true)
        verify { view.setBackgroundResource(R.color.md_theme_light_error) }
    }

    @Test
    fun `setDynamicBackground clears the background when not eligible`() {
        val view = mockk<View>(relaxed = true)
        setDynamicBackground(view, false)
        verify { view.background = null }
    }

    // =====================================================
    // TextView localization binding adapters
    // =====================================================

    @Test
    fun `setLocalizedDewormingLocation localizes a known value`() {
        val ctx = contextWithArray(R.array.deworming_location_options, arrayOf("L-School"))
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setLocalizedDewormingLocation("School")

        verify { view.text = "L-School" }
    }

    @Test
    fun `setAHDLocalization localizes a known value`() {
        val ctx = contextWithArray(R.array.ahd_place_options, arrayOf("A-School"))
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setAHDLocalization("School")

        verify { view.text = "A-School" }
    }

    @Test
    fun `setVHNDLocalization localizes a known value`() {
        val ctx = contextWithArray(R.array.place_of_vhsnc, arrayOf("V-Anganwadi"))
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setVHNDLocalization("Anganwadi Centre")

        verify { view.text = "V-Anganwadi" }
    }

    @Test
    fun `setSaasBahuSamalonLocalization localizes a known value`() {
        val ctx = contextWithArray(R.array.place_array, arrayOf("S-HWC"))
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setSaasBahuSamalonLocalization("HWC")

        verify { view.text = "S-HWC" }
    }

    @Test
    fun `setUWINLocalization localizes a known value and wraps it in the place label`() {
        val ctx = contextWithArray(R.array.place_of_delivery_options, arrayOf("U-HWC"))
        every { ctx.getString(R.string.place_label, "U-HWC") } returns "Place: U-HWC"
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setUWINLocalization("HWC")

        verify { view.text = "Place: U-HWC" }
    }

    // =====================================================
    // TextView.setLocalizedGender()
    // =====================================================

    @Test
    fun `setLocalizedGender localizes male`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.male) } returns "Male"
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setLocalizedGender("male")

        verify { view.text = "Male" }
    }

    @Test
    fun `setLocalizedGender localizes female`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.female) } returns "Female"
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setLocalizedGender("FEMALE")

        verify { view.text = "Female" }
    }

    @Test
    fun `setLocalizedGender localizes transgender`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.transgender) } returns "Transgender"
        val view = mockk<TextView>(relaxed = true)
        every { view.context } returns ctx

        view.setLocalizedGender("Transgender")

        verify { view.text = "Transgender" }
    }

    @Test
    fun `setLocalizedGender falls back to the raw value for anything else`() {
        val view = mockk<TextView>(relaxed = true)
        view.setLocalizedGender("Other")
        verify { view.text = "Other" }
    }

    @Test
    fun `setLocalizedGender falls back to an empty string for null`() {
        val view = mockk<TextView>(relaxed = true)
        view.setLocalizedGender(null)
        verify { view.text = "" }
    }
}
