package org.piramalswasthya.sakhi.ui.home_activity.incentives

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
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
import org.piramalswasthya.sakhi.helpers.Konstants.additionalIncentivetoAshaSGovt
import org.piramalswasthya.sakhi.helpers.Konstants.adolescentHealth
import org.piramalswasthya.sakhi.helpers.Konstants.antaraProg
import org.piramalswasthya.sakhi.helpers.Konstants.ashaIncetiveJSY
import org.piramalswasthya.sakhi.helpers.Konstants.ashaMonthlyRActivity
import org.piramalswasthya.sakhi.helpers.Konstants.childHealth
import org.piramalswasthya.sakhi.helpers.Konstants.familyPlanning
import org.piramalswasthya.sakhi.helpers.Konstants.immunization
import org.piramalswasthya.sakhi.helpers.Konstants.maternalHealth
import org.piramalswasthya.sakhi.helpers.Konstants.ncd
import org.piramalswasthya.sakhi.helpers.Konstants.umbrellaProgrames
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects


@AndroidEntryPoint
class IncentivesFragment : Fragment() {

    private var _binding: FragmentIncentivesBinding? = null
    private val binding: FragmentIncentivesBinding
        get() = _binding!!


    private var incentiveDomainList: List<IncentiveDomain> = mutableListOf()

    private val viewModel: IncentivesViewModel by viewModels()

    var selectedMonth: String = ""

    var selectedYear: String = ""

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
        val pageWidth = 345
        val pageHeight = 400
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)

        var canvas = page.canvas
        val paint = Paint()
//        paint.isAntiAlias = true
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

        // Draw the box

        var x = 10
        var y = 50
        val columnWidth = 130
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
            "Name of the ASHA: ${viewModel.currentUser?.name}",
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
            "Claim from: 01 - $selectedMonth - $selectedYear to ${
                getCurrentDateString()
            }",
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
        val activityItems = ArrayList<IncentiveDomainDTO>()
        activityItems.add(0,addDataIntoIncentiveDomain(childHealth,"Providing HBNC up to 42 days after birth / discharge from SNCU"))
        activityItems.add(1,addDataIntoIncentiveDomain(childHealth,"Incentive to ASHA for quarterly visits of HBYC"))
        activityItems.add(2,addDataIntoIncentiveDomain(childHealth,"Incentive to ASHA for follow up of SNCU discharge babies and for follow up of LBW babies"))
        activityItems.add(3,addDataIntoIncentiveDomain(childHealth,"ASHA incentive for referral of SAM cases to NRC and for follow up of discharged SAM children from NRC"))
        activityItems.add(4,addDataIntoIncentiveDomain(childHealth,"Child Death Reporting"))
        activityItems.add(5,addDataIntoIncentiveDomain(childHealth,"Incentive for quarterly mothers' meeting under MAA"))
        activityItems.add(6,addDataIntoIncentiveDomain(childHealth,"Incentive for National Deworming Day for mobilising out of school children"))
        activityItems.add(7,addDataIntoIncentiveDomain(childHealth,"Incentive for IDCF for prophylactic distribution of ORS to family with under-five children"))
        activityItems.add(8,addDataIntoIncentiveDomain(childHealth,"National Iron Plus Incentive for mobilizing WRA (non pregnant & non lactating Women 20-49 years)"))
        activityItems.add(9,addDataIntoIncentiveDomain(childHealth,"NIPI Incentive for mobilizing children and/or ensuring compliance and reporting (6-59 months"))
        activityItems.add(10,addDataIntoIncentiveDomain(immunization,"ASHA incentive for ensuring for full immunization(0-1 year)"))
        activityItems.add(11,addDataIntoIncentiveDomain(immunization,"Ensuring for complete immunization (1 - 2 years)"))
        activityItems.add(12,addDataIntoIncentiveDomain(immunization,"Ensuring for DPT immunization(5 years)"))
        activityItems.add(13,addDataIntoIncentiveDomain(immunization,"mobilization of children in every session site"))
        activityItems.add(14,addDataIntoIncentiveDomain(maternalHealth,"ASHA incentive for ANC registration within 1st Trimester"))
        activityItems.add(15,addDataIntoIncentiveDomain(maternalHealth,"ASHA incentive for ensuring Full ANC"))
        activityItems.add(16,addDataIntoIncentiveDomain(maternalHealth,"ASHA incentive for Comprehensive Abortion Care"))
        activityItems.add(17,addDataIntoIncentiveDomain(maternalHealth,"ASHA incentive for Community Based Distribution of Misoprostol"))
        activityItems.add(18,addDataIntoIncentiveDomain(maternalHealth,"ASHA incentive for ensuring Institutional delivery of identified HRP"))
        activityItems.add(19,addDataIntoIncentiveDomain(maternalHealth,"EPMSMA - instt delivery"))
        activityItems.add(20,addDataIntoIncentiveDomain(maternalHealth,"EPMSMA - IF HRPW identified at PMSMA Site"))
        activityItems.add(21,addDataIntoIncentiveDomain(maternalHealth,"ASHA Incentive for maternal death reporting "))
        activityItems.add(22,addDataIntoIncentiveDomain(maternalHealth,"For ensuring early registration of pregnancy and opening of bank account of beneficiary"))
        activityItems.add(23,addDataIntoIncentiveDomain(maternalHealth,"Rs. 100/ for ensuring one ANC by MO in the third trimester"))
        activityItems.add(24,addDataIntoIncentiveDomain(maternalHealth,"For motivating Institutional delivery of the beneficiary"))
        activityItems.add(25,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 1st Delivery(Rural) for Antenatal Component"))
        activityItems.add(26,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 1st Delivery(Rural) for facilitating Institutional Delivery"))
        activityItems.add(27,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 2nd Delivery(Rural) for Antenatal Component"))
        activityItems.add(28,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 2nd Delivery(Rural) for facilitating Institutional Delivery"))
        activityItems.add(29,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 3rd Delivery(Rural) for Antenatal Component"))
        activityItems.add(30,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 3rd Delivery(Rural) for facilitating Institutional Delivery"))
        activityItems.add(31,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 4th Delivery(Rural) for Antenatal Component"))
        activityItems.add(32,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive for ASHA for 4th Delivery(Rural) for facilitating Institutional Delivery"))
        activityItems.add(33,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive(Urban) for Antenatal Component"))
        activityItems.add(34,addDataIntoIncentiveDomain(ashaIncetiveJSY,"JSY incentive (Urban)for facilitating Institutional Delivery"))
        activityItems.add(35,addDataIntoIncentiveDomain(familyPlanning,antaraProg))
        activityItems.add(36,addDataIntoIncentiveDomain(familyPlanning,antaraProg))
        activityItems.add(37,addDataIntoIncentiveDomain(familyPlanning,antaraProg))
        activityItems.add(38,addDataIntoIncentiveDomain(familyPlanning,antaraProg))
        activityItems.add(39,addDataIntoIncentiveDomain(familyPlanning,"Ensuring 3 years gap between 1st and 2nd child birth"))
        activityItems.add(40,addDataIntoIncentiveDomain(familyPlanning,"Ensuring delaying 2 years for first child birth after marriage"))
        activityItems.add(41,addDataIntoIncentiveDomain(familyPlanning,"Ensuring limiting after 2 child"))
        activityItems.add(42,addDataIntoIncentiveDomain(familyPlanning,"For motivating a woman for PPIUCD insertion "))
        activityItems.add(43,addDataIntoIncentiveDomain(familyPlanning,"For motivating PAIUCD"))
        activityItems.add(44,addDataIntoIncentiveDomain(familyPlanning,"Home Delivery Contraceptive-Condom"))
        activityItems.add(45,addDataIntoIncentiveDomain(familyPlanning,"Home Delivery Contraceptive-Oral Pills"))
        activityItems.add(46,addDataIntoIncentiveDomain(familyPlanning,"Home Delivery Contraceptive-EC"))
        activityItems.add(47,addDataIntoIncentiveDomain(familyPlanning,"For motivating Female sterilization"))
        activityItems.add(48,addDataIntoIncentiveDomain(familyPlanning,"For motivating a woman for PPS"))
        activityItems.add(49,addDataIntoIncentiveDomain(familyPlanning,"For Motivating Minilap"))
        activityItems.add(50,addDataIntoIncentiveDomain(familyPlanning,"For motivating Male sterilization"))
        activityItems.add(51,addDataIntoIncentiveDomain(familyPlanning,"ASHA incentive for updation of EC survey before each MPV campaign"))
        activityItems.add(52,addDataIntoIncentiveDomain(familyPlanning,"Incentive to mobilize Saas Bahu Sammelan"))
        activityItems.add(53,addDataIntoIncentiveDomain(familyPlanning,"Incentive for Distributing Naye Pahel Kit"))
        activityItems.add(54,addDataIntoIncentiveDomain(adolescentHealth,"Menstrual Hygiene – For selling sanitary napkin."))
        activityItems.add(55,addDataIntoIncentiveDomain(adolescentHealth,"Incentive for selection and support of Peer Educator"))
        activityItems.add(56,addDataIntoIncentiveDomain(adolescentHealth,"Incentive for mobilizing adolescents and community for AHD"))
        activityItems.add(57,addDataIntoIncentiveDomain(ashaMonthlyRActivity,"a) Mobilizing and attending Village Health and Nutrition Day"))
        activityItems.add(58,addDataIntoIncentiveDomain(ashaMonthlyRActivity,"b) Convening and guiding monthly Village Health Sanitation and Nutrition meeting"))
        activityItems.add(59,addDataIntoIncentiveDomain(ashaMonthlyRActivity,"c)Attending PHC Review meeting"))
        activityItems.add(60,addDataIntoIncentiveDomain(ashaMonthlyRActivity,"Activities Like:\n" +
                "I) Line listing of household done at beginning of the year and \n" +
                "updated after every six months. \n" +
                "II) Maintaining village health register and supporting universal \n" +
                "registration of births and deaths"))
        activityItems.add(61,addDataIntoIncentiveDomain(umbrellaProgrames,"NPCB - For ensuring Treatment of Cataract surgery in Govt. facility"))
        activityItems.add(62,addDataIntoIncentiveDomain(umbrellaProgrames,"NPCB - For ensuring Treatment of Cataract surgery in Private facility"))
        activityItems.add(63,addDataIntoIncentiveDomain(umbrellaProgrames,"Incentive for completion of 6 month treatment of DSTB"))
        activityItems.add(64,addDataIntoIncentiveDomain(umbrellaProgrames,"Incentive for completion of 18 month treatment of DRTB"))
        activityItems.add(65,addDataIntoIncentiveDomain(umbrellaProgrames,"Informant incentive for active case finding"))
        activityItems.add(66,addDataIntoIncentiveDomain(umbrellaProgrames,"NLEP – Sensitization ASHA Incentive for training on Leprosy"))
        activityItems.add(67,addDataIntoIncentiveDomain(umbrellaProgrames,"NLEP - Incentive for case detection"))
        activityItems.add(68,addDataIntoIncentiveDomain(umbrellaProgrames,"NLEP - for ensuring complete treatment of PB cases."))
        activityItems.add(69,addDataIntoIncentiveDomain(umbrellaProgrames,"NLEP - for ensuring complete treatment of MB cases."))
        activityItems.add(70,addDataIntoIncentiveDomain(umbrellaProgrames,"Partial Incentives to the ASHAs for Leprosy suspects"))
        activityItems.add(71,addDataIntoIncentiveDomain(umbrellaProgrames,"NVBDCP -For malaria slide collection."))
        activityItems.add(72,addDataIntoIncentiveDomain(umbrellaProgrames,"NVBDCP -For ensuring treatment of Malaria positive cases"))
        activityItems.add(73,addDataIntoIncentiveDomain(umbrellaProgrames,"ASHA incentive for referral of AES/JE cases to the nearest CHC/DH/Medical College"))
        activityItems.add(74,addDataIntoIncentiveDomain(umbrellaProgrames,"ASHA Incentive for Dengue and Chikungunya"))
        activityItems.add(75,addDataIntoIncentiveDomain(umbrellaProgrames,"NIDDCP – For testing 50 salt samples per month"))
        activityItems.add(76,addDataIntoIncentiveDomain(ncd,"Incentive for population enumeration,CBAC filling and mobilizing for NCD screening "))
        activityItems.add(77,addDataIntoIncentiveDomain(ncd,"Incentive for follow up and treatment compliance for 6 months for patients diagnosed with Hypertension,Diabetes,Mellitus & 3 common cancers(Oral, Breast,Carvical)"))
        activityItems.add(78,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"a) Line Listing of Adolscent and linkage with WIFS"))
        activityItems.add(79,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"b) Line Listing of Adolscent and linkage with WIFS"))
        activityItems.add(80,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"c)Line Listing of Screened children under RBSK by Mobile Health Team in her are"))
        activityItems.add(81,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"d)Facilitation of High Risk Pregnancy identification and line listing"))
        activityItems.add(82,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"e)Follow up of Full ANC with complete routine examination of each pregnant women"))
        activityItems.add(83,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"f)Mobilizing for screening of HIV of all pregnant women"))
        activityItems.add(84,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"g)Identification of Malaria/Dengue/JE cases and line listing"))
        activityItems.add(85,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"h)Identification of TB Cases and line listing"))
        activityItems.add(86,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"i)Updating of MCP Card and ensuring opening of bank A/C of beneficiary registered in her area"))
        activityItems.add(87,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"j)Participating in NCD Screening in her area"))
        activityItems.add(88,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"k)Ensuring supplement of IFA to under 5 children and line listing"))
        activityItems.add(89,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"l)Follow-up of full immunization with JE,MR,Rota Virus, Vitamin A, etc and line listing"))
        activityItems.add(90,addDataIntoIncentiveDomain(additionalIncentivetoAshaSGovt,"m)Identification of number of under 5 children with diarrhea traced and distributed ORS during the month and line listing"))


        for(i in activityItems.indices) {

            for(j in items.indices) {
                if (items[j].group == activityItems[i].group && items[j].description == activityItems[i].description) {
                    activityItems[i] = items[j]
                }
            }
        }

        var currentGroup = ""
        var slNo = 1
        y += rowHeight



        var total = 0L
        activityItems.forEach {
            if (y > pageHeight-15) {
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
                    y += 10
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
                    ""
                )
                total += it.amountClaimed
                currentGroup = it.group
                y += rowHeight
                if (slNo != 75) {
                    slNo += 1
                }


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
            text = "Name of the ASHA : ${viewModel.currentUser?.name}",
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
            text = "Village: ${viewModel.locationRecord!!.village.name}",
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
        val fileName = "Incentives_" + selectedMonth + "_" + selectedYear + ".pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                        document.close()

                        Snackbar.make(binding.root, "$fileName downloaded", Snackbar.LENGTH_LONG)
                            .setAction("Show File") {
                                showFile(uri)
                            }
                            .show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Failed to create file URI", Toast.LENGTH_LONG).show()
            }
        } else {
             val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, fileName)

             try {
            directory.mkdirs()
            document.writeTo(FileOutputStream(file))
            document.close()

            val snackbar = Snackbar.make(binding.root, "$fileName downloaded", Snackbar.LENGTH_LONG)
            snackbar.setAction("Show File") {
                showFile(file.toUri())
            }
            snackbar.show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, " ${e.message}", Toast.LENGTH_LONG).show()
        }
        }

        document.close()


    }

    private fun addDataIntoIncentiveDomain(groupName: String,description: String): IncentiveDomainDTO {
        return  IncentiveDomainDTO(0,groupName,"",description,"",0,0,0,"")
    }

    private fun showFile(uri: Uri) {
        val openFileIntent = Intent(Intent.ACTION_VIEW)
        openFileIntent.setDataAndType(
            uri,
            "application/*"
        )
        openFileIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
        val chooser = Intent.createChooser(openFileIntent, "Open with")

        if (openFileIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(chooser)
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
        val rowHeight = 20
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
            (y + rowHeight).toFloat(),
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


