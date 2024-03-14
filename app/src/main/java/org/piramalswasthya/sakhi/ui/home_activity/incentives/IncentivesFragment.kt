package org.piramalswasthya.sakhi.ui.home_activity.incentives

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IncentiveListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentIncentivesBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.setToEndOfTheDay
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveDomainDTO
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.drawMultilineText
import org.piramalswasthya.sakhi.utils.MonthYearPickerDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar


@AndroidEntryPoint
class IncentivesFragment : Fragment() {

    private var _binding: FragmentIncentivesBinding? = null
    private val binding: FragmentIncentivesBinding
        get() = _binding!!


    private var incentiveDomainList: List<IncentiveDomain> = mutableListOf()

    private val viewModel: IncentivesViewModel by viewModels()

    var selectedMonth: String = ""

    var selectedYear: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncentivesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // from month
        val fromMonth: Spinner = binding.fromMonthsSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            fromMonth.adapter = adapter
        }

//        fromMonth.setSelection(0)

        val myArrayList = ArrayList<Int>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (i in currentYear downTo 2020) {
            myArrayList.add(i)
        }

        val fromYear: Spinner = binding.fromYearsSpinner
        val fromYearsAdapter: ArrayAdapter<Int> =
            ArrayAdapter<Int>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                myArrayList
            )
        fromYear.adapter = fromYearsAdapter
        fromYear.setSelection(0)

        val toMonth: Spinner = binding.toMonthsSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            toMonth.adapter = adapter
        }
//        toMonth.setSelection(0)

        val toYear: Spinner = binding.toYearsSpinner

        toYear.adapter = fromYearsAdapter
        toYear.setSelection(0)

        val adapter = IncentiveListAdapter()
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvIncentive.addItemDecoration(divider)
        binding.rvIncentive.adapter = adapter
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setCalendarConstraints(
                    CalendarConstraints.Builder().setStart(Konstants.defaultTimeStamp)
                        .setEnd(System.currentTimeMillis())
                        .build()
                )
                .build()

        dateRangePicker.addOnPositiveButtonClickListener {
            viewModel.setRange(it.first, it.second)
        }

        lifecycleScope.launch {
            viewModel.incentiveList.collect {
                incentiveDomainList = it
                adapter.submitList(it)
                val activityList = it.map { it.activity }
                val pending = activityList.filter { !it.isPaid }.sumOf { it.rate }
                val processed = activityList.filter { it.isPaid }.sumOf { it.rate }
                binding.tvTotalPending.text = getString(R.string.incentive_pending, pending)
                binding.tvTotalProcessed.text = getString(R.string.incentive_processed, processed)
                binding.tvLastupdated.text =
                    getString(R.string.incentive_last_updated, viewModel.lastUpdated)
            }
        }

        binding.fetchData.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(
                Calendar.MONTH,
                resources.getStringArray(R.array.months).indexOf(fromMonth.selectedItem)
            )
            selectedMonth = fromMonth.selectedItem.toString()
            calendar.set(Calendar.YEAR, fromYear.selectedItem.toString().toInt())
            selectedYear = fromYear.selectedItem.toString()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.setToStartOfTheDay()
            val firstDay = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.setToEndOfTheDay()
            val lastDay = calendar.timeInMillis
            viewModel.setRange(firstDay, lastDay)
        }

        binding.tvTotalPending.setOnClickListener {
            downloadPdf()
        }

        binding.et1.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener { picker, i, i2, i3 ->
                run {
                    fromMonth.setSelection(i2)
//                    fromYear.se
                    binding.et1.setText("${resources.getStringArray(R.array.months)[i2]} $i")
                }
            }
            pd.show(requireFragmentManager(), "MonthYearPickerDialog1")
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__incentive,
                getString(R.string.incentive_fragment_title)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun downloadPdf() {


        val document = PdfDocument()

        val pageNumber = 1

        incentiveDomainList.forEach {

        }
        val pageWidth = 300
        val pageHeight = 400
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()
//        paint.isAntiAlias = true
        paint.isSubpixelText = false
        paint.color = Color.BLACK
        paint.textSize = 6f

        val textPaint = TextPaint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 6f
        textPaint.letterSpacing = 0.1f


        val boxPaint = Paint()
        boxPaint.color = Color.BLACK
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 0.3f

        // Draw the box

        var x = 10
        var y = 50
        val columnWidth = 100
        val rowHeight = 15

        var maxPages = incentiveDomainList.size / 20 + 2

        var currentPage = 1

        val lineGap = 7
        paint.textSize = 3.5f

        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            paint
        )

        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )

        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )

        canvas.drawLine(
            x.toFloat(), (pageHeight - lineGap).toFloat(),
            (pageWidth - 2 * x).toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )

        // MASTER CLAIM FORMAT FOR ASHAs
        y += lineGap
        canvas.drawText(
            "MASTER CLAIM FORMAT FOR ASHAs",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap

        //(TO BE SUBMITTED ON MONTHLY BASIS BY AN ASHA)

        canvas.drawText(
            "(TO BE SUBMITTED ON MONTHLY BASIS BY AN ASHA)",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap

        //Name of the ASHA:_____________________________
        canvas.drawText(
            "Name of the ASHA:_____________________________",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap
        //Name of the BPHC:_____________________________
        canvas.drawText(
            "Name of the BPHC:_____________________________",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap
        //Name of HI: ___________________________________
        canvas.drawText(
            "Name of HI: ___________________________________",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap
        //FY: ___________________________________________
        canvas.drawText(
            "FY: ___________________________________________",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap
        //Claim from: _______________ to ___________________
        canvas.drawText(
            "Claim from: _______________ to ___________________",
            (x + columnWidth).toFloat(),
            y.toFloat(),
            paint
        )
        canvas.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (y + lineGap).toFloat(),
            paint
        )
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (y + lineGap).toFloat(),
            paint
        )
        y += lineGap

        canvas.drawLine(x.toFloat(), y.toFloat(), x.toFloat(), (y + rowHeight).toFloat(), paint)
        canvas.drawLine(
            (pageWidth - 2 * x).toFloat(),
            y.toFloat(),
            (pageWidth - 2 * x).toFloat(),
            (y + rowHeight).toFloat(),
            paint
        )

        textPaint.textSize = 3.5f

        drawItemBox(
            canvas, x, y, textPaint, boxPaint,
            "Slno",
            "Activity",
            "Parameter for payment",
            "Rate Rs.",
            "No of Claims",
            "Amount Claimed",
            "Amount Approved(For Office use only)",
            "FMR Code",
            "Remarks (if any)"
        )

        x = 10
        val items: List<IncentiveDomainDTO> = viewModel.mapToView(incentiveDomainList)
        var currentGroup = ""
        var slNo = 1
        y += rowHeight

        var total = 0L
        items.forEach {
            if (y > pageHeight) {
                document.finishPage(page)
                currentPage++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                page = document.startPage(pageInfo)
                y = rowHeight
            } else {
                if (currentGroup.contentEquals(it.group)) {
                    //
                } else {
                    canvas.drawMultilineText(
                        text = it.group,
                        textPaint = textPaint,
                        width = pageWidth - 2 * x,
                        alignment = Layout.Alignment.ALIGN_CENTER,
                        x = x.toFloat(),
                        y = y.toFloat(),
                        start = 0
                    )
                    y += rowHeight
                }

                drawItemBox(
                    canvas,
                    x,
                    y,
                    textPaint,
                    boxPaint,
                    slNo.toString(),
                    it.description,
                    it.paymentParam,
                    it.rate.toString(),
                    it.noOfClaims.toString(),
                    it.amountClaimed.toString(),
                    "",
                    it.fmrCode ?: "",
                    "lia"
                )
                total += it.amountClaimed
                y += rowHeight
                currentGroup = it.group
                slNo += 1
            }
        }

        drawItemBox(
            canvas, x, y, textPaint, boxPaint,
            "",
            "Total",
            "",
            "",
            "",
            total.toString(),
            "",
            "",
            ""
        )
        // Finish the page
        document.finishPage(page)

        // Start a new page
        val pageInfo1 =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage + 1).create()
        val page1 = document.startPage(pageInfo1)

        val canvas1 = page1.canvas
        y = 50 // Reset y position


        canvas1.drawLine(
            x.toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            paint
        )

        canvas1.drawLine(
            x.toFloat(), y.toFloat(),
            x.toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )

        canvas1.drawLine(
            (pageWidth - 2 * x).toFloat(), y.toFloat(),
            (pageWidth - 2 * x).toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )

        canvas1.drawLine(
            x.toFloat(), (pageHeight - lineGap).toFloat(),
            (pageWidth - 2 * x).toFloat(), (pageHeight - lineGap).toFloat(),
            paint
        )
        x = 20
        canvas1.drawMultilineText(
            text = "Activity wise claim forms along with supporting documents are also enclosed as per guideline.",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )

        y += rowHeight
        canvas1.drawMultilineText(
            text = "Cetify that, all claims are genuine and services are rendered by me regarding the activities against which the claim submitted. Kindly make the payment.",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Yours faithfully,",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Name of the ASHA",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Account No:",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Bank Name & Branch Name:",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Contact No:",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight
        canvas1.drawMultilineText(
            text = "Village:",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight
        canvas1.drawMultilineText(
            text = "SC Name:",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Certify that the claims mentioned above are correct.",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight


        canvas1.drawMultilineText(
            text = "Signature of ASHA SUPERVISOR",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Signature of ANM",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "For office use only",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "An amount of Rs________________(Rupees_________only) approved for payment of ASHA incentive for the period from____________to_____________and the amount is debited to the account through DBT.",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = "Signature of ABPM\t\tSignature of BAM\t\tSignature of BCM\t\tSignature of BPM\t\tSignature of SDM & HO",
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        // Finish the second page
        document.finishPage(page1)

        // You can continue with more pages if needed

        // Save the PDF file
        val fileName = "Incentives_" + selectedMonth + "_" + selectedYear + ".pdf"
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            document.writeTo(FileOutputStream(file))
            val snackbar = Snackbar.make(binding.root, "$fileName downloaded", Snackbar.LENGTH_LONG)

            snackbar.setAction("Show File") {
                showFile(file.toUri())
            }

            snackbar.show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        document.close()


    }

    private fun showFile(uri: Uri) {
        // Create an Intent to open the file
        val openFileIntent = Intent(Intent.ACTION_VIEW)
        openFileIntent.setDataAndType(
            uri,
            "application/*"
        ) // Adjust the MIME type as per your file type

        // Check if there's an app to handle this intent
        if (openFileIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(openFileIntent)
        } else {
            Toast.makeText(
                requireContext(),
                "cant open this file check in downloads",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun drawItemBox(
        canvas: Canvas,
        x_val: Int,
        y: Int,
        textPaint: TextPaint,
        boxPaint: Paint,
        s: String,
        s1: String,
        s2: String,
        s3: String,
        s4: String,
        s5: String,
        s6: String,
        s7: String,
        s8: String
    ) {
        val colWidth = 15
        val rowHeight = 15
        var x = 10
        canvas.drawMultilineText(
            text = s,
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = colWidth,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        boxPaint.color = Color.GRAY
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += colWidth


        canvas.drawMultilineText(
            text = s1,
            textPaint = textPaint,
            width = 5 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 5 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 5 * colWidth


        canvas.drawMultilineText(
            text = s2,
            textPaint = textPaint,
            width = 2 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 2 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth

        canvas.drawMultilineText(
            text = s3,
            textPaint = textPaint,
            width = colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += colWidth

        canvas.drawMultilineText(
            text = s4,
            textPaint = textPaint,
            width = colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += colWidth

        canvas.drawMultilineText(
            text = s5,
            textPaint = textPaint,
            width = 2 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 2 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth

        canvas.drawMultilineText(
            text = s6,
            textPaint = textPaint,
            width = 2 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 2 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth

        canvas.drawMultilineText(
            text = s7,
            textPaint = textPaint,
            width = 2 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 2 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth

        canvas.drawMultilineText(
            text = s8,
            textPaint = textPaint,
            width = 2 * colWidth,
            alignment = Layout.Alignment.ALIGN_CENTER,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 2 * colWidth).toFloat(),
            (y + rowHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth


    }


}