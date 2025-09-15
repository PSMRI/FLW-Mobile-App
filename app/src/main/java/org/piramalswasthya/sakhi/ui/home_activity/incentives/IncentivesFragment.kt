package org.piramalswasthya.sakhi.ui.home_activity.incentives

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IncentiveGroupedAdapter
import org.piramalswasthya.sakhi.databinding.FragmentIncentivesBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.setToEndOfTheDay
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.IncentiveActivityDomain
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveDomainDTO
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.drawMultilineText
import org.piramalswasthya.sakhi.utils.MonthYearPickerDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects
import kotlin.math.max


@AndroidEntryPoint
class IncentivesFragment : Fragment() {

    private var _binding: FragmentIncentivesBinding? = null
    private val binding: FragmentIncentivesBinding
        get() = _binding!!


    private var incentiveDomainList: List<IncentiveDomain> = mutableListOf()
    private var incentiveRecordList: List<IncentiveDomain> = mutableListOf()
    private var incentivesActivityList: List<IncentiveActivityDomain> = mutableListOf()

    private val viewModel: IncentivesViewModel by viewModels()

    var selectedMonth: String = ""

    var selectedYear: String = ""

    private lateinit var groupedAdapter: IncentiveGroupedAdapter

    private val PERMISSION_REQUEST_CODE = 792

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

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

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
        //  fromYear.setSelection(0)

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


        groupedAdapter = IncentiveGroupedAdapter { activityId, activityName ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getRecordsForActivity(activityId).collect { records ->

                    val bundle = bundleOf(
                        "records" to ArrayList(records),
                        "activityName" to activityName
                    )
                    setFragmentResult("records_key", bundle)

                    findNavController().navigate(
                        R.id.action_incentivesFragment_to_incentiveDetailFragment,
                    )

                    cancel()
                }
            }
        }

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvIncentive.addItemDecoration(divider)
        binding.rvIncentive.adapter = groupedAdapter
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
            viewModel.items.collect {
                incentivesActivityList = it

            }

        }

        lifecycleScope.launch {
            viewModel.incentiveList.collect {

                incentiveRecordList = it
                val activityList = it.map { it.activity }
                val pending = activityList.filter { !it.isPaid }.sumOf { it.rate }
                val processed = activityList.filter { it.isPaid }.sumOf { it.rate }
                binding.tvTotalPending.text = getString(R.string.incentive_pending, pending)
                binding.tvTotalProcessed.text = getString(R.string.incentive_processed, processed)
                binding.tvLastupdated.text =
                    getString(R.string.incentive_last_updated, viewModel.lastUpdated)

            }


        }


        lifecycleScope.launch {

            viewModel.groupedIncentiveList.collect { groupedList ->
                groupedAdapter.submitList(groupedList)
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
            askPermissions()
        }

        binding.et1.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener { picker, i, i2, i3 ->
                run {
                    fromMonth.setSelection(i2)
                    fromYear.setSelection(myArrayList.indexOf(i))
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
        val pageWidth = 375
        val pageHeight = 580
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)

        var canvas = page.canvas
        val paint = Paint()
        paint.isSubpixelText = false
        paint.color = Color.BLACK
        paint.textSize = 6f

        val textPaint = TextPaint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 6f
        textPaint.letterSpacing = 0.2f


        val boxPaint = Paint()
        boxPaint.color = Color.BLACK
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 0.3f


        var x = 10
        var y = 50
        val columnWidth = 130
        val margin = 15
        val rowHeight = 20

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

        y += lineGap
        y += lineGap
        textPaint.textSize = 5f
        canvas.drawText(
            context?.getString(R.string.asha_incentive_master_claim_form) ?: "",
            (pageWidth / 2 - textPaint.measureText(context?.getString(R.string.asha_incentive_master_claim_form)) / 2),
            y.toFloat(),
            textPaint
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


        textPaint.textSize = 4f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("To,", margin.toFloat(), y.toFloat(), textPaint)
        y += 10

        canvas.drawText("SDM&HO or i/c Block PHC", margin.toFloat(), y.toFloat(), textPaint)
        y += 10

        canvas.drawText(
            "-----------------------------------",
            margin.toFloat(),
            y.toFloat(),
            textPaint
        )
        y += 15

        val fromDate = "01 - $selectedMonth - $selectedYear"
        val toDate = getCurrentDateString()

        canvas.drawText(
            "Sub: Submission of ASHA incentive claim for the period from",
            margin.toFloat(),
            y.toFloat(),
            textPaint
        )
        y += 10

        canvas.drawText("$fromDate to $toDate", margin.toFloat() + 20, y.toFloat(), textPaint)
        y += 15


        canvas.drawText("Sir/Madam,", margin.toFloat(), y.toFloat(), textPaint)

        y += 15

        fun drawWrappedText(
            canvas: Canvas,
            text: String,
            paint: TextPaint,
            x: Int,
            y: Int,
            maxWidth: Int
        ): Int {
            val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(x.toFloat(), y.toFloat())
            staticLayout.draw(canvas)
            canvas.restore()

            return staticLayout.height
        }

        val formalText =
            "With reference to the subject cited above, I have the honour to submit the " +
                    "ASHA incentives claims for the period from $fromDate to $toDate as per statement mentioned below."


        val availableWidth = pageWidth - 2 * margin
        val usedHeight =
            drawWrappedText(canvas, formalText, textPaint, margin, y, availableWidth - 10)
        y += usedHeight + 10

        y += 15
        canvas.drawLine(x.toFloat(), y.toFloat(), (pageWidth - 2 * x).toFloat(), y.toFloat(), paint)
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
            "FMR Code",
            "No of Claims",
            "Amount Claimed (Rs.)",
            "Documents Submitted",
            "Amount Approved(For Office use only)",
            "Remarks (if any)",
            20
        )

        x = 10
        val items: List<IncentiveDomainDTO> = viewModel.mapToDomainDTO(incentivesActivityList)
        //viewModel.mapToView(incentiveDomainList,incentiveRecordList)
        val activityItems = ArrayList<IncentiveDomainDTO>()
        /* activityItems.add(
             0,
             addDataIntoIncentiveDomain(
                 childHealth,
                 context?.getString(R.string.providing_hbnc_up_to_days_after_birth_discharge_from_sncu)?:""
             )
         )
         activityItems.add(
             1,
             addDataIntoIncentiveDomain(
                 childHealth,
                 getString(R.string.incentive_to_asha_for_quarterly_visits_of_hbyc)
             )
         )

         activityItems.add(
             2,
             addDataIntoIncentiveDomain(
                 childHealth,
                 getString(R.string.incentive_to_asha_for_follow_up_of_sncu_discharge_babies_and_for_follow_up_of_lbw_babies)
             )
         )
         activityItems.add(
             3,
             addDataIntoIncentiveDomain(
                 childHealth,
                 getString(R.string.asha_incentive_for_referral_of_sam_cases_to_nrc_and_for_follow_up_of_discharged_sam_children_from_nrc)
             )
         )
         activityItems.add(4, addDataIntoIncentiveDomain(childHealth,
             getString(R.string.child_death_reporting)))
         activityItems.add(
             5,
             addDataIntoIncentiveDomain(
                 childHealth,
                 getString(R.string.incentive_for_e_hbnc_only_for_cachar)
             )
         )
         activityItems.add(
             6,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "Incentive for quarterly mothers' meeting under MAA"
             )
         )
         activityItems.add(
             7,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "Incentive for quarterly mothers' meeting under MAA"
             )
         )
         activityItems.add(
             8,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "Incentive for National Deworming Day for mobilising out of school children"
             )
         )
         activityItems.add(
             9,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "Incentive for IDCF for prophylactic distribution of ORS to family with under-five children"
             )
         )
         activityItems.add(
             10,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "National Iron Plus Incentive for mobilizing WRA (non pregnant & non lactating Women 20-49 years)"
             )
         )
         activityItems.add(
             11,
             addDataIntoIncentiveDomain(
                 childHealth,
                 "NIPI Incentive for mobilizing children and/or ensuring compliance and reporting (6-59 months"
             )
         )
         activityItems.add(
             12,
             addDataIntoIncentiveDomain(
                 immunization,
                 "ASHA incentive for ensuring for full immunization(0-1 year)"
             )
         )
         activityItems.add(
             13,
             addDataIntoIncentiveDomain(
                 immunization,
                 "Ensuring for complete immunization (1 - 2 years)"
             )
         )
         activityItems.add(
             14,
             addDataIntoIncentiveDomain(immunization, "Ensuring for DPT immunization(5 years)")
         )
         activityItems.add(
             15,
             addDataIntoIncentiveDomain(
                 immunization,
                 "mobilization of children in every session site"
             )
         )
         activityItems.add(
             16,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA incentive for ANC registration within 1st Trimester"
             )
         )
         activityItems.add(
             17,
             addDataIntoIncentiveDomain(maternalHealth, "ASHA incentive for ensuring Full ANC")
         )
         activityItems.add(
             18,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA incentive for Comprehensive Abortion Care"
             )
         )
         activityItems.add(
             19,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA incentive for Community Based Distribution of Misoprostol"
             )
         )
         activityItems.add(
             20,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA incentive for ensuring Institutional delivery of identified HRP"
             )
         )
         activityItems.add(21, addDataIntoIncentiveDomain(maternalHealth, "EPMSMA - instt delivery"))
         activityItems.add(
             22,
             addDataIntoIncentiveDomain(maternalHealth, "EPMSMA - IF HRPW identified at PMSMA Site")
         )
         activityItems.add(
             23,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA Incentive for maternal death reporting "
             )
         )
         activityItems.add(
             24,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "For ensuring early registration of pregnancy and opening of bank account of beneficiary"
             )
         )
         activityItems.add(
             25,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "Rs. 100/ for ensuring one ANC by MO in the third trimester"
             )
         )
         activityItems.add(
             26,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "For motivating Institutional delivery of the beneficiary"
             )
         )
         activityItems.add(
             27,
             addDataIntoIncentiveDomain(
                 maternalHealth,
                 "ASHA incentive for identification for High Risk cases in the post natal period"
             )
         )
         activityItems.add(
             28,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 1st Delivery(Rural) for Antenatal Component"
             )
         )
         activityItems.add(
             29,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 1st Delivery(Rural) for facilitating Institutional Delivery"
             )
         )
         activityItems.add(
             30,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 2nd Delivery(Rural) for Antenatal Component"
             )
         )
         activityItems.add(
             31,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 2nd Delivery(Rural) for facilitating Institutional Delivery"
             )
         )
         activityItems.add(
             32,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 3rd Delivery(Rural) for Antenatal Component"
             )
         )
         activityItems.add(
             33,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 3rd Delivery(Rural) for facilitating Institutional Delivery"
             )
         )
         activityItems.add(
             34,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 4th Delivery(Rural) for Antenatal Component"
             )
         )
         activityItems.add(
             35,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive for ASHA for 4th Delivery(Rural) for facilitating Institutional Delivery"
             )
         )
         activityItems.add(
             36,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive(Urban) for Antenatal Component"
             )
         )
         activityItems.add(
             37,
             addDataIntoIncentiveDomain(
                 ashaIncetiveJSY,
                 "JSY incentive (Urban)for facilitating Institutional Delivery"
             )
         )
         activityItems.add(38, addDataIntoIncentiveDomain(familyPlanning, antaraProg))
         activityItems.add(39, addDataIntoIncentiveDomain(familyPlanning, antaraProg))
         activityItems.add(40, addDataIntoIncentiveDomain(familyPlanning, antaraProg))
         activityItems.add(41, addDataIntoIncentiveDomain(familyPlanning, antaraProg))
         activityItems.add(
             42,
             addDataIntoIncentiveDomain(
                 familyPlanning,
                 "ASHA incentive for accompanying the client for Injectable\n" +
                         "MPA(Antara Prog)administration"
             )
         )
         activityItems.add(
             43,
             addDataIntoIncentiveDomain(
                 familyPlanning,
                 "Ensuring 3 years gap between 1st and 2nd child birth"
             )
         )
         activityItems.add(
             44,
             addDataIntoIncentiveDomain(
                 familyPlanning,
                 "Ensuring delaying 2 years for first child birth after marriage"
             )
         )
         activityItems.add(
             45,
             addDataIntoIncentiveDomain(familyPlanning, "Ensuring limiting after 2 child")
         )
         activityItems.add(
             46,
             addDataIntoIncentiveDomain(
                 familyPlanning,
                 "For motivating a woman for PPIUCD insertion "
             )
         )
         activityItems.add(47, addDataIntoIncentiveDomain(familyPlanning, "For motivating PAIUCD"))
         activityItems.add(
             48,
             addDataIntoIncentiveDomain(familyPlanning, "Home Delivery Contraceptive-Condom")
         )
         activityItems.add(
             49,
             addDataIntoIncentiveDomain(familyPlanning, "Home Delivery Contraceptive-Oral Pills")
         )
         activityItems.add(
             50,
             addDataIntoIncentiveDomain(familyPlanning, "Home Delivery Contraceptive-EC")
         )
         activityItems.add(
             51,
             addDataIntoIncentiveDomain(familyPlanning, "For motivating Female sterilization")
         )
         activityItems.add(
             52,
             addDataIntoIncentiveDomain(familyPlanning, "For motivating a woman for PPS")
         )
         activityItems.add(53, addDataIntoIncentiveDomain(familyPlanning, "For Motivating Minilap"))
         activityItems.add(
             54,
             addDataIntoIncentiveDomain(familyPlanning, "For motivating Male sterilization")
         )
         activityItems.add(
             55,
             addDataIntoIncentiveDomain(
                 familyPlanning,
                 "ASHA incentive for updation of EC survey before each MPV campaign"
             )
         )
         activityItems.add(
             56,
             addDataIntoIncentiveDomain(familyPlanning, "Incentive to mobilize Saas Bahu Sammelan")
         )
         activityItems.add(
             57,
             addDataIntoIncentiveDomain(familyPlanning, "Incentive for Distributing Naye Pahel Kit")
         )
         activityItems.add(
             58,
             addDataIntoIncentiveDomain(
                 adolescentHealth,
                 "Menstrual Hygiene – For selling sanitary napkin."
             )
         )
         activityItems.add(
             59,
             addDataIntoIncentiveDomain(
                 adolescentHealth,
                 "Incentive for selection and support of Peer Educator"
             )
         )
         activityItems.add(
             60,
             addDataIntoIncentiveDomain(
                 adolescentHealth,
                 "Incentive for mobilizing adolescents and community for AHD"
             )
         )
         activityItems.add(
             61,
             addDataIntoIncentiveDomain(
                 ashaMonthlyRActivity,
                 "a) Mobilizing and attending Village Health and Nutrition Day.  \n" +
                         "b) Convening and guiding monthly Village Health Sanitation \n" +
                         "and Nutrition meeting.\n" +
                         "c)Attending PHC Review meeting. \n" +
                         "Activities Like:\n" +
                         "I) Line listing of household done at beginning of the year and \n" +
                         "updated after every six months. \n" +
                         "II) Maintaining village health register and supporting universal \n" +
                         "registration of births and deaths.\n" +
                         "III) Preparation of due list of children to be immunized\n" +
                         "updated on monthly basis.\n" +
                         "IV) Preparation of list of ANC beneficiaries to be updated on\n" +
                         "monthly basis and submission of micro birth plan for each of\n" +
                         "the PW. (State priority)\n" +
                         "V) Preparation of list of eligible couples updated on monthly\n" +
                         "basis."
             )
         )
         *//* activityItems.add(
             58,
             addDataIntoIncentiveDomain(
                 ashaMonthlyRActivity,
                 "b) Convening and guiding monthly Village Health Sanitation and Nutrition meeting"
             )
         )
         activityItems.add(
             59,
             addDataIntoIncentiveDomain(ashaMonthlyRActivity, "c)Attending PHC Review meeting")
         )
         activityItems.add(
             60, addDataIntoIncentiveDomain(
                 ashaMonthlyRActivity, "Activities Like:\n" +
                         "I) Line listing of household done at beginning of the year and \n" +
                         "updated after every six months. \n" +
                         "II) Maintaining village health register and supporting universal \n" +
                         "registration of births and deaths"
             )
         )*//*
        activityItems.add(
            62,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NPCB - For ensuring Treatment of Cataract surgery in Govt. facility"
            )
        )
        activityItems.add(
            63,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NPCB - For ensuring Treatment of Cataract surgery in Private facility"
            )
        )
        activityItems.add(
            64,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Incentive for completion of 6 month treatment of DSTB"
            )
        )
        activityItems.add(
            65,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Incentive for completion of treatment of DRTB"
            )
        )
        activityItems.add(
            66,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Informant incentive "
            )
        )
        activityItems.add(
            67,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Incentive for Providing TPT"
            )
        )
        activityItems.add(
            68,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Incentive for facilitating Bank Account Seeding of Patients for \n" +
                        "NPY"
            )
        )
        activityItems.add(
            69,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Incentive for House to House Survey (Active Case Finding)"
            )
        )
        activityItems.add(
            70,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NLEP – Sensitization ASHA Incentive for training on Leprosy"
            )
        )
        activityItems.add(
            71,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                " NLEP - Incentive for case detection. "
            )
        )
        activityItems.add(
            72,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NLEP - for ensuring complete treatment of PB cases."
            )
        )
        activityItems.add(
            73,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NLEP - for ensuring complete treatment of MB cases."
            )
        )
        activityItems.add(
            74,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "Partial Incentives to the ASHAs for Leprosy suspects"
            )
        )
        activityItems.add(
            75,
            addDataIntoIncentiveDomain(umbrellaProgrames, "NVBDCP -For malaria slide collection.")
        )
        activityItems.add(
            76,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NVBDCP -For ensuring treatment of Malaria positive cases"
            )
        )
        activityItems.add(
            77,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "ASHA incentive for referral of AES/JE cases to the nearest CHC/DH/Medical College"
            )
        )
        activityItems.add(
            78,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "ASHA Incentive for Dengue and Chikungunya"
            )
        )
        activityItems.add(
            79,
            addDataIntoIncentiveDomain(
                umbrellaProgrames,
                "NIDDCP – For testing 50 salt samples per month"
            )
        )
        activityItems.add(
            80,
            addDataIntoIncentiveDomain(
                ncd,
                "Incentive for population enumeration,CBAC filling and mobilizing for NCD screening "
            )
        )
        activityItems.add(
            81,
            addDataIntoIncentiveDomain(
                ncd,
                "Incentive for follow up and treatment compliance for 6 months for patients diagnosed with Hypertension,Diabetes,Mellitus & 3 common cancers(Oral, Breast,Carvical)"
            )
        )
        activityItems.add(
            82,
            addDataIntoIncentiveDomain(
                other_Incentive,
                "Incentive for ABHA ID Creation"
            )
        )
        activityItems.add(
            83,
            addDataIntoIncentiveDomain(
                other_Incentive,
                "Mobile bill reimbursement of ASHA"
            )
        )
        activityItems.add(
            84,
            addDataIntoIncentiveDomain(
                additionalIncentivetoAshaSGovt,
                "a) Line Listing of Adolscent and linkage with WIFS \n" +
                        "b) Line Listing of Adolscent and linkage with WIFS \n" +
                        "c)Line Listing of Screened children under RBSK by Mobile Health Team in her are \n" +
                        "d)Facilitation of High Risk Pregnancy identification and line listing \n" +
                        "e)Follow up of Full ANC with complete routine examination of each pregnant women \n" +
                        "f)Mobilizing for screening of HIV of all pregnant women \n" +
                        "g)Identification of Malaria/Dengue/JE cases and line listing \n" +
                        "h)Identification of TB Cases and line listing \n" +
                        "i)Updating of MCP Card and ensuring opening of bank A/C of beneficiary registered in her area \n" +
                        "j)Participating in NCD Screening in her area \n" +
                        "k)Ensuring supplement of IFA to under 5 children and line listing \n" +
                        "l)Follow-up of full immunization with JE,MR,Rota Virus, Vitamin A, etc and line listing \n" +
                        "m)Identification of number of under 5 children with diarrhea traced and distributed ORS during the month and line listing "
            )
        )
        */
        var currentGroup = ""
        var slNo = 1
        y += rowHeight

        fun getTextHeight(text: String, paint: TextPaint, width: Int): Int {
            val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()

            return staticLayout.height
        }


        var total = 0L
        items.forEach { it ->

            if (y > pageHeight - 15) {
                document.finishPage(page)
                currentPage++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = rowHeight
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

            }
            if (currentGroup.contentEquals(it.group)) {

            } else {
                canvas.drawMultilineText(
                    text = it.groupName,
                    textPaint = textPaint,
                    width = pageWidth - 2 * x,
                    alignment = Layout.Alignment.ALIGN_CENTER,
                    x = x.toFloat(),
                    y = y.toFloat(),
                    start = 0
                )
                y += 10
            }
            val descriptionHeight = getTextHeight(it.description, textPaint, 100)
            val dynamicRowHeight = max(rowHeight, descriptionHeight + 8)

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
                it.fmrCode ?: "",
                it.noOfClaims.toString(),
                it.amountClaimed.toString(),
                "",
                "",
                "",
                dynamicRowHeight
            )
            total += it.amountClaimed
            currentGroup = it.group
            y += dynamicRowHeight

            if (slNo != 84) {
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
            "",
            total.toString(),
            "",
            "",
            "",
            20

        )

        document.finishPage(page)


        val pageInfo1 =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage + 1).create()
        val page1 = document.startPage(pageInfo1)

        val canvas1 = page1.canvas
        y = 50


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
            text = getString(R.string.activity_wise_claim_forms_along_with_supporting_documents_are_also_enclosed_as_per_guideline),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )

        y += rowHeight
        canvas1.drawMultilineText(
            text = getString(R.string.cetify_that_all_claims_are_genuine_and_services_are_rendered_by_me_regarding_the_activities_against_which_the_claim_submitted_kindly_make_the_payment),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.yours_faithfully),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.name_of_the_asha, viewModel.currentUser?.name),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.account_no),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.bank_name_branch_name),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.contact_no),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight
        canvas1.drawMultilineText(
            text = getString(R.string.village, viewModel.locationRecord!!.village.name),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight
        canvas1.drawMultilineText(
            text = getString(R.string.sc_name),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.certify_that_the_claims_mentioned_above_are_correct),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight


        canvas1.drawMultilineText(
            text = getString(R.string.signature_of_asha_supervisor),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.signature_of_anm),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.signature_of_cho),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.for_office_use_only),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_CENTER,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        canvas1.drawMultilineText(
            text = getString(R.string.an_amount_of_rs_rupees_only_approved_for_payment_of_asha_incentive_for_the_period_from_to_and_the_amount_is_debited_to_the_account_through_dbt),
            textPaint = textPaint,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            width = pageWidth - 2 * x,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        y += rowHeight

        val sigAbpm = context?.getString(R.string.signature_of_abpm)
        val sigBam = context?.getString(R.string.signature_of_bam)
        val sigBcm = context?.getString(R.string.signature_of_bcm)
        val sigBpm = context?.getString(R.string.signature_of_bpm)
        val sigSdm = context?.getString(R.string.signature_of_sdm_ho)
        val totalWidth = pageWidth - 2 * margin
        val signauture_columnWidth = totalWidth / 5

        val labels = listOf(sigAbpm, sigBam, sigBcm, sigBpm, sigSdm)

        labels.forEachIndexed { index, text ->
            val xPos =
                margin + index * signauture_columnWidth + (signauture_columnWidth - textPaint.measureText(
                    text
                )) / 2
            canvas1.drawText(text!!, xPos, y.toFloat(), textPaint)
        }


        y += rowHeight


        document.finishPage(page1)


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

    private fun addDataIntoIncentiveDomain(
        groupName: String,
        description: String
    ): IncentiveDomainDTO {
        return IncentiveDomainDTO(0, groupName, "", description, "", "", 0, 0, 0, "")
    }

    private fun showFile(uri: Uri) {

        val openFileIntent = Intent(Intent.ACTION_VIEW)
        openFileIntent.setDataAndType(
            uri,
            "application/*"
        )


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
        s8: String,
        s9: String,
        dynamicHeight: Int
    ) {
        val colWidth = 15
        val rowHeight = 30
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
            (y + dynamicHeight).toFloat(),
            boxPaint
        )
        x += colWidth


        canvas.drawMultilineText(
            text = s1,
            textPaint = textPaint,
            width = 8 * colWidth,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            x = x.toFloat(),
            y = y.toFloat(),
            start = 0
        )
        canvas.drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + 8 * colWidth).toFloat(),
            (y + dynamicHeight).toFloat(),
            boxPaint
        )
        x += 8 * colWidth


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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
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
            (y + dynamicHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth

        canvas.drawMultilineText(
            text = s9,
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
            (y + dynamicHeight).toFloat(),
            boxPaint
        )
        x += 2 * colWidth


    }

    private fun askPermissions() {

        val sdkversion = Build.VERSION.SDK_INT

        if (sdkversion >= 33) {

            downloadPdf()

        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        Objects.requireNonNull<Any>(
                            requireContext()
                        ) as Context, permission
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    requestPermissions(
                        permissions,
                        PERMISSION_REQUEST_CODE
                    )
                    return
                } else {
                    downloadPdf()
                }
            }

        }
    }

}

private fun getCurrentDateString(): String {
    val calendar = Calendar.getInstance()
    val mdFormat = SimpleDateFormat("dd - MMMM - yyyy", Locale.ENGLISH)
    return mdFormat.format(calendar.time)
}


