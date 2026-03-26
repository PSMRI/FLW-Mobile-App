package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.cbac

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentCbacBinding
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.form.LeprosyFormViewModel
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form.NCDReferDialogViewModel
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.form.TBScreeningFormViewModel
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.HelperUtil.findFragmentActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.jvm.java


@AndroidEntryPoint
class CbacFragment : Fragment() {


    private val binding by lazy { FragmentCbacBinding.inflate(layoutInflater) }

    private val viewModel: CbacViewModel by viewModels()
    val referViewModel: NCDReferDialogViewModel by viewModels()

    private val isInFillMode: Boolean by lazy {
        viewModel.cbacId == 0
    }


    private var isSuspected: Boolean = false

    private val viewModelTbScreening: TBScreeningFormViewModel by viewModels()
    private val viewModelLeprosyScreening: LeprosyFormViewModel by viewModels()

    private val alertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.missing_field)).create()
    }
    private var isReferralDialogShown = false
    var referralForReason = "Suspected NCD Case"
    var referType = "NCD"

    private val raAlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.alert))
//            .setMessage(context?.getString(R.string.ncd_sus_valid))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
    private val ast1AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.suspected_hrp_and_ncd_case))
            .setMessage(resources.getString(R.string.tb_suspected_alert))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private var asreferAlertDialog: AlertDialog? = null

    private fun buildAsReferAlertDialog(): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(referType)
            .setMessage(getString(R.string.ncd_refer_alert))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                showReferralDialog(
                    fragment = this,
                    type = referType,
                    reason = referralForReason,
                    benId = viewModel.benId,
                    cbacId = viewModel.cbacId.toLong(),
                    cbacViewModel = viewModel
                )
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private val ast2AlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.suspected_hrp_case))
            .setMessage(resources.getString(R.string.tb_suspected_family_alert))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private val ast0AlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.alert))
            .setMessage(
                resources.getString(R.string.suspected_ncd_case_please_visit_nearest_hwc_or_call)
            )
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    private var dialogAlreadyShown = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLeprosySuspected.observe(viewLifecycleOwner) { suspected ->
            if (suspected && isInFillMode &&
                !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.LEPROSY) &&
                !dialogAlreadyShown
            ) {
                dialogAlreadyShown = true
                viewModelLeprosyScreening.saveLeprosySuspectedFormDirectlyfromCbac()
                referralForReason = "Suspected Leprosy case"
                referType = "LEPROSY"
                asreferAlertDialog = buildAsReferAlertDialog()
                asreferAlertDialog?.show()


            } else {
//                asreferAlertDialog = buildAsReferAlertDialog()
                asreferAlertDialog?.dismiss()

            }
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("REFERRAL_DONE")
            ?.observe(viewLifecycleOwner) { typeName ->

                val type = CbacViewModel.ReferralType.valueOf(typeName)
                viewModel.markReferralCompleted(type)
            }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("REFERRAL_RESULT")
            ?.observe(viewLifecycleOwner) { json ->

                val referral = Gson().fromJson(json, ReferalCache::class.java)
                viewModel.addReferral(referral)

            }


        viewModel.showReferralDialog.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow  && !isReferralDialogShown) {
                isReferralDialogShown = true
                showDialog()
            }
        }

        viewModelTbScreening.state.observe(viewLifecycleOwner) {
            when (it) {
                TBScreeningFormViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.cbac_screening_submitted), Toast.LENGTH_SHORT
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }
                else -> {
                    Timber.d("IDLE!")
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {

                CbacViewModel.State.LOADING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbCbac.visibility = View.VISIBLE
                    Timber.d("IDLE!")
                }

                CbacViewModel.State.IDLE -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    if (isInFillMode) setUpFill()
                    else setUpView()
                }

                CbacViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbCbac.visibility = View.VISIBLE
                }

                CbacViewModel.State.SAVE_FAIL -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    Timber.d("Ran into error! Cbac data not saved!")
                    viewModel.resetState()
                }

                CbacViewModel.State.SAVE_SUCCESS -> {
                    Timber.d("CBAC form saved successfully!")
                    viewModel.resetState()
                    if (viewModel.referralCache != null) {
                        lifecycleScope.launch {
                            viewModel.cbacRepo.updateReferStatus(viewModel.benId, true)
                        }
                    }
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                CbacViewModel.State.MISSING_FIELD -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    alertDialog.setTitle(resources.getString(R.string.missing_field))
                    alertDialog.setMessage(viewModel.missingFieldString)
                    alertDialog.show()
                }

                else -> {
                    Timber.d("IDLE!")
                }
            }
        }

        binding.benId.text = viewModel.benId.toString()

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        viewModel.raAgeScore.observe(viewLifecycleOwner) {
            binding.ddAgeScore.text = it
        }
        viewModel.raSmokeScore.observe(viewLifecycleOwner) {
            binding.ddSmokeScore.text = it
        }
        viewModel.raAlcoholScore.observe(viewLifecycleOwner) {
            binding.ddAlcoholScore.text = it
        }
        viewModel.raWaistScore.observe(viewLifecycleOwner) {
            binding.ddWaistScore.text = it
        }
        viewModel.raPaScore.observe(viewLifecycleOwner) {
            binding.ddPaScore.text = it
        }

        viewModel.raFhScore.observe(viewLifecycleOwner) {
            binding.ddFhScore.text = it
            val totalScore = viewModel.raTotalScore.value
                ?.substringAfter(": ")
                ?.toIntOrNull() ?: 0
            referralForReason = "Suspected NCD Case"
            referType = "NCD"

            handleNcdSusBottomInfoDisplay(totalScore)

        }

        viewModel.raTotalScore.observe(viewLifecycleOwner) {
            val score = it.substring(it.lastIndexOf(' ') + 1).toInt()
            handleRaScoreAlert(score)
            binding.cbacTvRaTotalScore.text = it
        }


        binding.cbNoneOfThese.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbacHistb.rbNo.isChecked = true
                viewModel.setHisTb(2)
                binding.cbacCoughing.rbNo.isChecked = true
                viewModel.setCoughing(2)
                binding.cbacBlsputum.rbNo.isChecked = true
                viewModel.setBloodSputum(2)
                binding.cbacFeverwks.rbNo.isChecked = true
                viewModel.setFeverWks(2)
                binding.cbacLsweight.rbNo.isChecked = true
                viewModel.setLsWt(2)
                binding.cbacNtswets.rbNo.isChecked = true
                viewModel.setNtSwets(2)
                binding.cbacRecurrentUlceration.rbNo.isChecked = true
                viewModel.setRecurrentUlceration(2)
                binding.cbacRecurrentCloudy.rbNo.isChecked = true
                viewModel.setRecurrentCloudy(2)
                binding.cbacRecurrentDiffcultyReading.rbNo.isChecked = true
                viewModel.setDiffReading(2)
                binding.cbacRecurrentPainEyes.rbNo.isChecked = true
                viewModel.setPainEyes(2)
                binding.cbacRecurrentRednessEyes.rbNo.isChecked = true
                viewModel.setRedEyes(2)
                binding.cbacRecurrentDiffHearing.rbNo.isChecked = true
                viewModel.setDiffHearing(2)
                binding.cbacBreath.rbNo.isChecked = true
                viewModel.setBreathe(2)
                binding.cbacRecurrentTingling.rbNo.isChecked = true
                viewModel.setTing(2)
                binding.cbacHifits.rbNo.isChecked = true
                viewModel.setHisFits(2)
                binding.cbacDifmouth.rbNo.isChecked = true
                viewModel.setDiffMouth(2)
                binding.cbacHeald.rbNo.isChecked = true
                viewModel.setHealed(2)
                binding.cbacVoice.rbNo.isChecked = true
                viewModel.setVoice(2)
                binding.cbacAnyGrowth.rbNo.isChecked = true
                viewModel.setAnyGrowth(2)
                binding.cbacAnyWhite.rbNo.isChecked = true
                viewModel.setAnyWhite(2)
                binding.cbacPainWhileChewing.rbNo.isChecked = true
                viewModel.setPainChew(2)
                binding.cbacAnyHyperPigmented.rbNo.isChecked = true
                viewModel.setHyperPig(2)
                binding.cbacAnyThickendSkin.rbNo.isChecked = true
                viewModel.setThickSkin(2)
                binding.cbacAnyNodulesSkin.rbNo.isChecked = true
                viewModel.setNoduleSkin(2)
                binding.cbacRecurrentNumbness.rbNo.isChecked = true
                viewModel.setNumb(2)
                binding.cbacClawingOfFingers.rbNo.isChecked = true
                viewModel.setClaw(2)
                binding.cbacTinglingOrNumbness.rbNo.isChecked = true
                viewModel.setTingNumb(2)
                binding.cbacInabilityCloseEyelid.rbNo.isChecked = true
                viewModel.setCloseEyelid(2)
                binding.cbacDiffHoldingObjects.rbNo.isChecked = true
                viewModel.setHoldObj(2)
                binding.cbacWeeknessInFeet.rbNo.isChecked = true
                viewModel.setWeakFeet(2)
                binding.cbacLumpbrest.rbNo.isChecked = true
                viewModel.setLumpB(2)
                binding.cbacNipple.rbNo.isChecked = true
                viewModel.setNipple(2)
                binding.cbacBreast.rbNo.isChecked = true
                viewModel.setBreast(2)
                binding.cbacBlperiods.rbNo.isChecked = true
                viewModel.setBlP(2)

                binding.cbacBlmenopause.rbNo.isChecked = true
                viewModel.setBlM(2)
                binding.tvBlMenopause.visibility = View.GONE

                binding.cbacBlintercorse.rbNo.isChecked = true
                viewModel.setBlI(2)
                binding.cbacFouldis.rbNo.isChecked = true
                viewModel.setFoulD(2)
                binding.cbacUnsteady.rbNo.isChecked = true
                viewModel.setUnsteady(2)
                binding.cbacPdRm.rbNo.isChecked = true
                viewModel.setPdRm(2)
                binding.cbacNhop.rbNo.isChecked = true
                viewModel.setNhop(2)
                binding.cbacForgetNames.rbNo.isChecked = true
                viewModel.setForgetNames(2)

                binding.cbacFhTb.rbNo.isChecked = true
                viewModel.setFhTb(2)
                binding.cbacTakingTbDrug.rbNo.isChecked = true
                viewModel.setTakingTbDrug(2)
            }else{
                binding.cbacHistb.cbacEdRg.clearCheck()
                viewModel.setHisTb(0)
                binding.cbacCoughing.cbacEdRg.clearCheck()
                viewModel.setCoughing(0)
                binding.cbacBlsputum.cbacEdRg.clearCheck()
                viewModel.setBloodSputum(0)
                binding.cbacFeverwks.cbacEdRg.clearCheck()
                viewModel.setFeverWks(0)
                binding.cbacLsweight.cbacEdRg.clearCheck()
                viewModel.setLsWt(0)
                binding.cbacNtswets.cbacEdRg.clearCheck()
                viewModel.setNtSwets(0)

                binding.cbacRecurrentUlceration.cbacEdRg.clearCheck()
                viewModel.setRecurrentUlceration(0)
                binding.cbacRecurrentCloudy.cbacEdRg.clearCheck()
                viewModel.setRecurrentCloudy(0)
                binding.cbacRecurrentDiffcultyReading.cbacEdRg.clearCheck()
                viewModel.setDiffReading(0)
                binding.cbacRecurrentPainEyes.cbacEdRg.clearCheck()
                viewModel.setPainEyes(0)
                binding.cbacRecurrentRednessEyes.cbacEdRg.clearCheck()
                viewModel.setRedEyes(0)
                binding.cbacRecurrentDiffHearing.cbacEdRg.clearCheck()
                viewModel.setDiffHearing(0)
                binding.cbacBreath.cbacEdRg.clearCheck()
                viewModel.setBreathe(0)
                binding.cbacRecurrentTingling.cbacEdRg.clearCheck()
                viewModel.setTing(0)
                binding.cbacHifits.cbacEdRg.clearCheck()
                viewModel.setHisFits(0)
                binding.cbacDifmouth.cbacEdRg.clearCheck()
                viewModel.setDiffMouth(0)
                binding.cbacHeald.cbacEdRg.clearCheck()
                viewModel.setHealed(0)
                binding.cbacVoice.cbacEdRg.clearCheck()
                viewModel.setVoice(0)
                binding.cbacAnyGrowth.cbacEdRg.clearCheck()
                viewModel.setAnyGrowth(0)
                binding.cbacAnyWhite.cbacEdRg.clearCheck()
                viewModel.setAnyWhite(0)
                binding.cbacPainWhileChewing.cbacEdRg.clearCheck()
                viewModel.setPainChew(0)
                binding.cbacAnyHyperPigmented.cbacEdRg.clearCheck()
                viewModel.setHyperPig(0)
                binding.cbacAnyThickendSkin.cbacEdRg.clearCheck()
                viewModel.setThickSkin(0)
                binding.cbacAnyNodulesSkin.cbacEdRg.clearCheck()
                viewModel.setNoduleSkin(0)
                binding.cbacRecurrentNumbness.cbacEdRg.clearCheck()
                viewModel.setNumb(0)
                binding.cbacClawingOfFingers.cbacEdRg.clearCheck()
                viewModel.setClaw(0)
                binding.cbacTinglingOrNumbness.cbacEdRg.clearCheck()
                viewModel.setTingNumb(0)
                binding.cbacInabilityCloseEyelid.cbacEdRg.clearCheck()
                viewModel.setCloseEyelid(0)
                binding.cbacDiffHoldingObjects.cbacEdRg.clearCheck()
                viewModel.setHoldObj(0)
                binding.cbacWeeknessInFeet.cbacEdRg.clearCheck()
                viewModel.setWeakFeet(0)
                binding.cbacLumpbrest.cbacEdRg.clearCheck()
                viewModel.setLumpB(0)
                binding.cbacNipple.cbacEdRg.clearCheck()
                viewModel.setNipple(0)
                binding.cbacBreast.cbacEdRg.clearCheck()
                viewModel.setBreast(0)
                binding.cbacBlperiods.cbacEdRg.clearCheck()
                viewModel.setBlP(0)

                binding.cbacBlmenopause.cbacEdRg.clearCheck()
                viewModel.setBlM(0)
                binding.tvBlMenopause.visibility = View.GONE

                binding.cbacBlintercorse.cbacEdRg.clearCheck()
                viewModel.setBlI(0)
                binding.cbacFouldis.cbacEdRg.clearCheck()
                viewModel.setFoulD(0)
                binding.cbacUnsteady.cbacEdRg.clearCheck()
                viewModel.setUnsteady(0)
                binding.cbacPdRm.cbacEdRg.clearCheck()
                viewModel.setPdRm(0)
                binding.cbacNhop.cbacEdRg.clearCheck()
                viewModel.setNhop(0)
                binding.cbacForgetNames.cbacEdRg.clearCheck()
                viewModel.setForgetNames(0)

                binding.cbacFhTb.cbacEdRg.clearCheck()
                viewModel.setFhTb(0)
                binding.cbacTakingTbDrug.cbacEdRg.clearCheck()
                viewModel.setTakingTbDrug(0)

            }
        }


    }

    private fun handleRaScoreAlert(score: Int) {
        if (
            !binding.actvAgeDropdown.text.isNullOrBlank() &&
            !binding.actvSmokeDropdown.text.isNullOrBlank() &&
            !binding.actvAlcoholDropdown.text.isNullOrBlank() &&
            !binding.actvWaistDropdown.text.isNullOrBlank() &&
            !binding.actvPaDropdown.text.isNullOrBlank() &&
            !binding.actvFhDropdown.text.isNullOrBlank()
        ) {
            if (score > 4) {
                raAlertDialog.setMessage(
                    resources.getString(R.string.refer_to_ncd_screening_day_vhsnd_hwc_for_ncd_screening_priority)
                )
            } else {
                raAlertDialog.setMessage(
                    resources.getString(R.string.refer_to_ncd_screening_day_vhsnd_hwc_for_ncd_screening_less_priority)
                )
            }
//            raAlertDialog.show()
        }

    }

    private fun handleAst2Alert() {
        if (
            binding.cbacFhTb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacTakingTbDrug.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (binding.cbacFhTb.rbYes.isChecked || binding.cbacTakingTbDrug.rbYes.isChecked) {
                ast2AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_or_inform_anm_mpw_to_tracing_of_all_family_members)
                )
                ast2AlertDialog.show()
            }
        }

    }

    private fun handleOldPeopleItemsAlert() {
        if (
            binding.cbacUnsteady.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacPdRm.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNhop.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacForgetNames.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacUnsteady.rbYes.isChecked ||
                binding.cbacPdRm.rbYes.isChecked ||
                binding.cbacNhop.rbYes.isChecked ||
                binding.cbacForgetNames.rbYes.isChecked
            ) {
                alertDialog.setTitle(resources.getString(R.string.alert))
                alertDialog.setMessage(
                    resources.getString(R.string.send_the_patient_to_moic_of_nearest_health_center_for_treatment)
                )
                alertDialog.show()
            }
        }

    }

    private fun handleAst1Alert() {
        if (
            binding.cbacHistb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacCoughing.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacBlsputum.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacFeverwks.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacLsweight.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNtswets.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacHistb.rbYes.isChecked ||
                binding.cbacCoughing.rbYes.isChecked ||
                binding.cbacBlsputum.rbYes.isChecked ||
                binding.cbacFeverwks.rbYes.isChecked ||
                binding.cbacLsweight.rbYes.isChecked ||
                binding.cbacNtswets.rbYes.isChecked
            ) {
                isSuspected = true
               /* ast1AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_and_collect_the_sputum_sample)
                )
                ast1AlertDialog.show()*/
                if (isInFillMode && !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.TB) ) {
                    referralForReason = "Suspected TB case"
                    referType = "TB"
                    asreferAlertDialog = buildAsReferAlertDialog()
                    asreferAlertDialog?.show()
                }

            } else {
                isSuspected = false
            }
        }

    }

    private fun handleAst0Alert() {
        if (
            binding.cbacHistb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacCoughing.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacBlsputum.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacFeverwks.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacLsweight.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNtswets.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacHistb.rbYes.isChecked ||
                binding.cbacCoughing.rbYes.isChecked ||
                binding.cbacBlsputum.rbYes.isChecked ||
                binding.cbacFeverwks.rbYes.isChecked ||
                binding.cbacLsweight.rbYes.isChecked ||
                binding.cbacNtswets.rbYes.isChecked
            ) {
                ast1AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_and_collect_the_sputum_sample)
                )
                ast1AlertDialog.show()
            }
        }

    }

    private fun handleNcdSusBottomInfoDisplay(score: Int) {

        if (score >= 4) {
            binding.ncdSusValidDisplay.visibility = View.VISIBLE
            viewModel.setFlagForNcd(true)
            if (score > 4) {
                if (isInFillMode && !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.NCD))
                    asreferAlertDialog = buildAsReferAlertDialog()
                asreferAlertDialog?.show()
            }
        } else {
            if (binding.ncdSusValidDisplay.visibility != View.GONE) {
                binding.ncdSusValidDisplay.visibility = View.GONE
                viewModel.setFlagForNcd(false)
            }
        }
    }

    private fun setUpView() {
        binding.btnSave.visibility = View.GONE
//        binding.noneCheck.visibility = View.GONE
//        binding.nonTxt.visibility = View.GONE
        viewModel.filledCbac.observe(viewLifecycleOwner) { cbac ->
            binding.etDate.setText(getDateFromLong(cbac.fillDate))
            setupRaView(cbac)
            setupEdView(cbac)
            setupRfCopdView(cbac)
            setupPhq2View(cbac)
        }

    }

    fun getDateFromLong(dateLong: Long): String? {
        if (dateLong == 0L) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return f.format(cal.time)


    }

    fun getLongFromDate(dateString: String?): Long {
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date = dateString?.let { f.parse(it) }
        return date?.time ?: 0L
    }

    private fun setUpFill() {
        binding.btnSave.setOnClickListener {
            viewModel.setFillDate(getLongFromDate(binding.etDate.text.toString()))
            viewModel.submitForm()
            if (isSuspected) {
                viewModelTbScreening.saveFormDirectlyfromCbac()
            }
        }
        binding.etDate.setText(getDateFromLong(System.currentTimeMillis()))
        val today = Calendar.getInstance()
        val thisYear = today.get(Calendar.YEAR)
        val thisMonth = today.get(Calendar.MONTH)
        val thisDay = today.get(Calendar.DAY_OF_MONTH)
        binding.etDate.setOnClickListener {
            val activity = binding.etDate.context.findFragmentActivity()
                ?: return@setOnClickListener
            val originalLocale = Locale.getDefault()
            HelperUtil.setEnLocaleForDatePicker(activity)

            val datePickerDialog = DatePickerDialog(
                it.context, { _, year, month, day ->
                    binding.etDate.setText(
                        "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                    )
                }, thisYear, thisMonth, thisDay
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            viewModel.minDate.observe(viewLifecycleOwner) {
                datePickerDialog.datePicker.minDate = it

            }
            datePickerDialog.show()
            datePickerDialog.setOnDismissListener {
                HelperUtil.setOriginalLocaleForDatePicker(activity,originalLocale)
            }
        }
        setupRaFill()
        setupEdFill()
        setupRfCopdFill()
        setupPhq2Fill()
    }

    private fun setupRaFill() {

        viewModel.gender.observe(viewLifecycleOwner) {
            when (it) {
                Gender.MALE -> {
                    binding.actvWaistDropdown.setSimpleItems(R.array.cbac_waist_mes_male)
                    binding.cbacLlEdWomen.visibility = View.GONE
                }

                else -> binding.actvWaistDropdown.setSimpleItems(R.array.cbac_waist_mes_female)
            }
        }
        viewModel.raAgeText.observe(viewLifecycleOwner) {
            binding.actvAgeDropdown.setText(it)
        }
        binding.actvSmokeDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_smoke),


                )
        )
        binding.actvPaDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_pa),


                )
        )
        binding.actvSmokeDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setSmoke(i)
        }
        binding.actvAlcoholDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setAlcohol(i)
        }
        binding.actvWaistDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setWaist(i)
        }
        binding.actvPaDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setPa(i)
        }
        binding.actvFhDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setFh(i)
        }
    }

    private fun setupRaView(cbac: CbacCache) {
//        binding.tilAgeDropdown.isEnabled = false
        binding.actvAgeDropdown.setText(resources.getStringArray(R.array.cbac_age)[cbac.cbac_age_posi - 1])
        binding.actvSmokeDropdown.setText(resources.getStringArray(R.array.cbac_smoke)[cbac.cbac_smoke_posi - 1])
        binding.actvAlcoholDropdown.setText(resources.getStringArray(R.array.cbac_alcohol)[cbac.cbac_alcohol_posi - 1])
        viewModel.gender.observe(viewLifecycleOwner) {
            when (it) {
                Gender.MALE -> {
                    binding.actvWaistDropdown.setText(resources.getStringArray(R.array.cbac_waist_mes_male)[cbac.cbac_waist_posi - 1])
                    binding.cbacLlEdWomen.visibility = View.GONE
                }

                else -> binding.actvWaistDropdown.setText(resources.getStringArray(R.array.cbac_waist_mes_female)[cbac.cbac_waist_posi - 1])

            }
        }
        binding.actvPaDropdown.setText(resources.getStringArray(R.array.cbac_pa)[cbac.cbac_pa_posi - 1])
        binding.actvFhDropdown.setText(resources.getStringArray(R.array.cbac_fh)[cbac.cbac_familyhistory_posi - 1])

        viewModel.setSmoke(cbac.cbac_smoke_posi - 1)
        viewModel.setAlcohol(cbac.cbac_alcohol_posi - 1)
        viewModel.setWaist(cbac.cbac_waist_posi - 1)
        viewModel.setPa(cbac.cbac_pa_posi - 1)
        viewModel.setFh(cbac.cbac_familyhistory_posi - 1)
    }

    private fun setupEdFill() {

        viewModel.ast1.observe(viewLifecycleOwner) {
            Timber.d("value of ast1 : $it")
            if (it > 0) {
                binding.tvTbSputumCollect.visibility = View.VISIBLE
                viewModel.setCollectSputum(1)
            } else {
                binding.tvTbSputumCollect.visibility = View.GONE
                viewModel.setCollectSputum(1)
            }
        }
        viewModel.ast2.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.tbSusValidDisplay.visibility = View.VISIBLE
                viewModel.setTraceAllMembers(1)
            } else {
                binding.tbSusValidDisplay.visibility = View.GONE
                viewModel.setTraceAllMembers(0)
            }
        }
        viewModel.astMoic.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.tvTbMoicVisit.visibility = View.VISIBLE
                viewModel.setReferMoic(1)
            } else {
                binding.tvTbMoicVisit.visibility = View.GONE
                viewModel.setReferMoic(0)
            }
        }
        binding.cbacFhTb.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setFhTb(1)
                R.id.rb_no -> viewModel.setFhTb(2)
            }
//            handleAst2Alert()
        }
        binding.cbacTakingTbDrug.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTakingTbDrug(1)
                R.id.rb_no -> viewModel.setTakingTbDrug(2)
            }
//            handleAst2Alert()
        }
        binding.cbacHistb.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHisTb(1)
                R.id.rb_no -> viewModel.setHisTb(2)
            }
//            handleAst1Alert()
        }
        binding.cbacCoughing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setCoughing(1)
                R.id.rb_no -> viewModel.setCoughing(2)
            }
//            handleAst1Alert()
        }
        binding.cbacBlsputum.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBloodSputum(1)
                R.id.rb_no -> viewModel.setBloodSputum(2)
            }
//            handleAst1Alert()
        }
        binding.cbacFeverwks.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setFeverWks(1)
                R.id.rb_no -> viewModel.setFeverWks(2)
            }
//            handleAst1Alert()
        }
        binding.cbacLsweight.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setLsWt(1)
                R.id.rb_no -> viewModel.setLsWt(2)
            }
//            handleAst1Alert()
        }
        binding.cbacNtswets.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNtSwets(1)
                R.id.rb_no -> viewModel.setNtSwets(2)
            }
            handleAst1Alert()

        }
        binding.cbacRecurrentUlceration.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRecurrentUlceration(1)
                R.id.rb_no -> viewModel.setRecurrentUlceration(2)
            }

        }
        binding.cbacRecurrentCloudy.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRecurrentCloudy(1)
                R.id.rb_no -> viewModel.setRecurrentCloudy(2)
            }
        }
        binding.cbacRecurrentDiffcultyReading.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffReading(1)
                R.id.rb_no -> viewModel.setDiffReading(2)
            }
        }

        binding.cbacRecurrentPainEyes.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPainEyes(1)
                R.id.rb_no -> viewModel.setPainEyes(2)
            }
        }
        binding.cbacRecurrentRednessEyes.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRedEyes(1)
                R.id.rb_no -> viewModel.setRedEyes(2)
            }
        }
        binding.cbacRecurrentDiffHearing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffHearing(1)
                R.id.rb_no -> viewModel.setDiffHearing(2)
            }
        }
        binding.cbacBreath.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBreathe(1)
                R.id.rb_no -> viewModel.setBreathe(2)
            }
        }
        binding.cbacRecurrentTingling.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTing(1)
                R.id.rb_no -> viewModel.setTing(2)
            }
        }
        binding.cbacHifits.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHisFits(1)
                R.id.rb_no -> viewModel.setHisFits(2)
            }
        }
        binding.cbacDifmouth.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffMouth(1)
                R.id.rb_no -> viewModel.setDiffMouth(2)
            }
        }
        binding.cbacHeald.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHealed(1)
                R.id.rb_no -> viewModel.setHealed(2)
            }
        }
        binding.cbacVoice.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setVoice(1)
                R.id.rb_no -> viewModel.setVoice(2)
            }
        }
        binding.cbacAnyGrowth.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setAnyGrowth(1)
                R.id.rb_no -> viewModel.setAnyGrowth(2)
            }
        }
        binding.cbacAnyWhite.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setAnyWhite(1)
                R.id.rb_no -> viewModel.setAnyWhite(2)
            }
        }
        binding.cbacPainWhileChewing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPainChew(1)
                R.id.rb_no -> viewModel.setPainChew(2)
            }
        }
        binding.cbacAnyHyperPigmented.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHyperPig(1)
                R.id.rb_no -> viewModel.setHyperPig(2)
            }
        }
        binding.cbacAnyThickendSkin.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setThickSkin(1)
                R.id.rb_no -> viewModel.setThickSkin(2)
            }
        }
        binding.cbacAnyNodulesSkin.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNoduleSkin(1)
                R.id.rb_no -> viewModel.setNoduleSkin(2)
            }
        }
        binding.cbacRecurrentNumbness.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNumb(1)
                R.id.rb_no -> viewModel.setNumb(2)
            }

        }
        binding.cbacClawingOfFingers.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setClaw(1)
                R.id.rb_no -> viewModel.setClaw(2)
            }

        }
        binding.cbacTinglingOrNumbness.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTingNumb(1)
                R.id.rb_no -> viewModel.setTingNumb(2)
            }

        }
        binding.cbacInabilityCloseEyelid.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setCloseEyelid(1)
                R.id.rb_no -> viewModel.setCloseEyelid(2)
            }
        }
        binding.cbacDiffHoldingObjects.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHoldObj(1)
                R.id.rb_no -> viewModel.setHoldObj(2)
            }

        }
        binding.cbacWeeknessInFeet.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setWeakFeet(1)
                R.id.rb_no -> viewModel.setWeakFeet(2)
            }
            viewModel.checkLeprosySymptoms()
        }
        binding.cbacLumpbrest.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setLumpB(1)

                }
                R.id.rb_no -> viewModel.setLumpB(2)
            }
        }
        binding.cbacNipple.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setNipple(1)
                }
                R.id.rb_no -> viewModel.setNipple(2)
            }
        }
        binding.cbacBreast.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setBreast(1)

                }
                R.id.rb_no -> viewModel.setBreast(2)
            }
        }
        binding.cbacBlperiods.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setBlP(1)
                }
                R.id.rb_no -> viewModel.setBlP(2)
            }
        }
        binding.cbacBlmenopause.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setBlM(1)
                    alertDialog.setTitle(resources.getString(R.string.alert))
                    alertDialog.setMessage(resources.getString(R.string.inform_asha_facilitator))
                    alertDialog.show()
                    binding.tvBlMenopause.visibility = View.VISIBLE
                }

                R.id.rb_no -> {
                    viewModel.setBlM(2)
                    binding.tvBlMenopause.visibility = View.GONE
                }
            }
        }
        binding.cbacBlintercorse.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setBlI(1)

                }
                R.id.rb_no -> viewModel.setBlI(2)
            }
        }
        binding.cbacFouldis.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes ->  {
                    viewModel.setFoulD(1)
                }
                R.id.rb_no -> viewModel.setFoulD(2)
            }
        }
        viewModel.age.observe(viewLifecycleOwner) {
            if (it >= 60) binding.cbacLlEdElderly.visibility = View.VISIBLE
            else if (binding.cbacLlEdElderly.visibility != View.GONE) binding.cbacLlEdElderly.visibility =
                View.GONE
        }
        binding.cbacUnsteady.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setUnsteady(1)
                R.id.rb_no -> viewModel.setUnsteady(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacPdRm.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPdRm(1)
                R.id.rb_no -> viewModel.setPdRm(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacNhop.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNhop(1)
                R.id.rb_no -> viewModel.setNhop(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacForgetNames.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setForgetNames(1)
                R.id.rb_no -> viewModel.setForgetNames(2)
            }
            handleOldPeopleItemsAlert()
        }
        //ED END
    }

    private fun setSelectedForRg(rg: RadioGroup, pos: Int) {
        pos.takeIf { it > 0 }?.let {
            rg.check(
                when (it) {
                    1 -> R.id.rb_yes
                    2 -> R.id.rb_no
                    else -> throw IllegalArgumentException("LALALLALALALALLALALALA")
                }
            )
        }
        rg.children.forEach {
            it.isClickable = isInFillMode
        }
    }

    private fun setupEdView(cbac: CbacCache) {
        setSelectedForRg(binding.cbacFhTb.cbacEdRg, cbac.cbac_sufferingtb_pos)
        setSelectedForRg(binding.cbacTakingTbDrug.cbacEdRg, cbac.cbac_antitbdrugs_pos)
        setSelectedForRg(binding.cbacHistb.cbacEdRg, cbac.cbac_tbhistory_pos)
        setSelectedForRg(binding.cbacCoughing.cbacEdRg, cbac.cbac_coughing_pos)
        setSelectedForRg(binding.cbacBlsputum.cbacEdRg, cbac.cbac_bloodsputum_pos)
        setSelectedForRg(binding.cbacFeverwks.cbacEdRg, cbac.cbac_fivermore_pos)
        setSelectedForRg(binding.cbacLsweight.cbacEdRg, cbac.cbac_loseofweight_pos)
        setSelectedForRg(binding.cbacNtswets.cbacEdRg, cbac.cbac_nightsweats_pos)
        setSelectedForRg(binding.cbacRecurrentUlceration.cbacEdRg, cbac.cbac_uicers_pos)
        setSelectedForRg(binding.cbacRecurrentCloudy.cbacEdRg, cbac.cbac_cloudy_posi)
        setSelectedForRg(binding.cbacRecurrentDiffcultyReading.cbacEdRg, cbac.cbac_diffreading_posi)
        setSelectedForRg(binding.cbacRecurrentPainEyes.cbacEdRg, cbac.cbac_pain_ineyes_posi)
        setSelectedForRg(binding.cbacRecurrentRednessEyes.cbacEdRg, cbac.cbac_redness_ineyes_posi)
        setSelectedForRg(binding.cbacRecurrentDiffHearing.cbacEdRg, cbac.cbac_diff_inhearing_posi)
        setSelectedForRg(binding.cbacBreath.cbacEdRg, cbac.cbac_sortnesofbirth_pos)
        setSelectedForRg(binding.cbacRecurrentTingling.cbacEdRg, cbac.cbac_tingling_palm_posi)
        setSelectedForRg(binding.cbacHifits.cbacEdRg, cbac.cbac_historyoffits_pos)
        setSelectedForRg(binding.cbacDifmouth.cbacEdRg, cbac.cbac_difficultyinmouth_pos)
        setSelectedForRg(binding.cbacHeald.cbacEdRg, cbac.cbac_growth_in_mouth_posi)
        setSelectedForRg(binding.cbacVoice.cbacEdRg, cbac.cbac_toneofvoice_pos)
        setSelectedForRg(binding.cbacAnyGrowth.cbacEdRg, cbac.cbac_growth_in_mouth_posi)
        setSelectedForRg(binding.cbacAnyWhite.cbacEdRg, cbac.cbac_white_or_red_patch_posi)
        setSelectedForRg(binding.cbacPainWhileChewing.cbacEdRg, cbac.cbac_Pain_while_chewing_posi)
        setSelectedForRg(
            binding.cbacAnyHyperPigmented.cbacEdRg, cbac.cbac_hyper_pigmented_patch_posi
        )
        setSelectedForRg(binding.cbacAnyThickendSkin.cbacEdRg, cbac.cbac_any_thickend_skin_posi)
        setSelectedForRg(binding.cbacAnyNodulesSkin.cbacEdRg, cbac.cbac_nodules_on_skin_posi)
        setSelectedForRg(
            binding.cbacRecurrentNumbness.cbacEdRg, cbac.cbac_tingling_or_numbness_posi
        )
        setSelectedForRg(binding.cbacClawingOfFingers.cbacEdRg, cbac.cbac_clawing_of_fingers_posi)
        setSelectedForRg(
            binding.cbacTinglingOrNumbness.cbacEdRg, cbac.cbac_tingling_or_numbness_posi
        )
        setSelectedForRg(
            binding.cbacInabilityCloseEyelid.cbacEdRg, cbac.cbac_inability_close_eyelid_posi
        )
        setSelectedForRg(binding.cbacDiffHoldingObjects.cbacEdRg, cbac.cbac_diff_holding_obj_posi)
        setSelectedForRg(binding.cbacWeeknessInFeet.cbacEdRg, cbac.cbac_weekness_in_feet_posi)
        setSelectedForRg(binding.cbacLumpbrest.cbacEdRg, cbac.cbac_lumpinbreast_pos)
        setSelectedForRg(binding.cbacNipple.cbacEdRg, cbac.cbac_blooddischage_pos)
        setSelectedForRg(binding.cbacBreast.cbacEdRg, cbac.cbac_changeinbreast_pos)
        setSelectedForRg(binding.cbacBlperiods.cbacEdRg, cbac.cbac_bleedingbtwnperiods_pos)
        setSelectedForRg(
            binding.cbacBlmenopause.cbacEdRg, cbac.cbac_bleedingaftermenopause_pos
        ).also {
            binding.tvBlMenopause.visibility =
                if (cbac.cbac_bleedingaftermenopause_pos == 1) View.VISIBLE else View.GONE
        }
        setSelectedForRg(binding.cbacBlintercorse.cbacEdRg, cbac.cbac_bleedingafterintercourse_pos)
        setSelectedForRg(binding.cbacFouldis.cbacEdRg, cbac.cbac_foulveginaldischarge_pos)
        viewModel.age.observe(viewLifecycleOwner) {
            if (it >= 60) binding.cbacLlEdElderly.visibility = View.VISIBLE
            else if (binding.cbacLlEdElderly.visibility != View.GONE) binding.cbacLlEdElderly.visibility =
                View.GONE
        }
        setSelectedForRg(binding.cbacUnsteady.cbacEdRg, cbac.cbac_feeling_unsteady_posi)
        setSelectedForRg(binding.cbacPdRm.cbacEdRg, cbac.cbac_suffer_physical_disability_posi)
        setSelectedForRg(binding.cbacNhop.cbacEdRg, cbac.cbac_needing_help_posi)
        setSelectedForRg(binding.cbacForgetNames.cbacEdRg, cbac.cbac_forgetting_names_posi)
    }


    private fun setupRfCopdFill() {
        binding.actvExposureDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_type_occupational_exposure),


                )
        )
        binding.actvFuelDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setFuelType(i)

        }
        binding.actvExposureDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setOccExposure(i)
            referralForReason = "Suspected COPD case"
            referType = "COPD"
            if (isInFillMode && !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.COPD)) {
                asreferAlertDialog = buildAsReferAlertDialog()
                asreferAlertDialog?.show()

            }

        }
    }

    private fun setupRfCopdView(cbac: CbacCache) {
        cbac.cbac_fuel_used_posi.takeIf { it > 0 }?.let {
            binding.actvFuelDropdown.setText(resources.getStringArray(R.array.cbac_type_Cooking_fuel)[it - 1])
        }
        cbac.cbac_occupational_exposure_posi.takeIf { it > 0 }?.let {
            binding.actvExposureDropdown.setText(resources.getStringArray(R.array.cbac_type_occupational_exposure)[it - 1])
        }

    }


    private fun setupPhq2Fill() {
        //PHQ2 START
        binding.actvLiDropdown.setOnItemClickListener { _, _, i, _ -> viewModel.setLi(i) }
        binding.actvFdDropdown.setOnItemClickListener { _, _, i, _ -> viewModel.setFd(i) }
        viewModel.phq2LittleInterestScore.observe(viewLifecycleOwner) {
            binding.ddLiScore.text = it
        }
        viewModel.phq2FeelDownDepScore.observe(viewLifecycleOwner) {
            binding.ddFdScore.text = it
        }
        viewModel.phq2TotalScore.observe(viewLifecycleOwner) {
            if (it.substring(it.lastIndexOf(' ') + 1).toInt() > 3) {
                binding.tvTbMoicVisit.visibility = View.VISIBLE
                if (isInFillMode && !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.GERIATRIC)) {
                    referralForReason = "further assessment for depression"
                    referType = "GERIATRIC"
                    asreferAlertDialog = buildAsReferAlertDialog()
                    asreferAlertDialog?.show()
                }
                viewModel.setFlagForPhQ2(true)
            } else {
                if (binding.tvTbMoicVisit.visibility != View.GONE) {
                    binding.tvTbMoicVisit.visibility = View.GONE
                    viewModel.setFlagForPhQ2(false)
                }
            }
            binding.cbacPhq2TotalScore.text = it
        }
    }


    private fun setupPhq2View(cbac: CbacCache) {
        cbac.cbac_little_interest_posi.takeIf { it > 0 }?.let {
            binding.actvLiDropdown.setText(resources.getStringArray(R.array.cbac_li)[it - 1])
        }
        cbac.cbac_feeling_down_posi.takeIf { it > 0 }?.let {
            binding.actvFdDropdown.setText(resources.getStringArray(R.array.cbac_fd)[it - 1])
        }
        binding.ddLiScore.text = cbac.cbac_little_interest_score.toString()
        binding.ddFdScore.text = cbac.cbac_feeling_down_score.toString()
        binding.cbacPhq2TotalScore.text =
            String.format(
                "%s%s%s", resources.getString(R.string.total_score_wihout_semi_colon),
                ": ", cbac.cbac_little_interest_score + cbac.cbac_feeling_down_score
            )
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ncd_eligibility,
                getString(R.string.cbac)
            )
        }
    }

    fun showDialog() {

            if (isInFillMode && !viewModel.isReferralAlreadyDone(CbacViewModel.ReferralType.HRP))
                referralForReason = "Part B2: Women Only – Symptom Present"
                referType = "HRP"
        asreferAlertDialog = buildAsReferAlertDialog()
        asreferAlertDialog?.show()


    }
    override fun onDestroy() {
        super.onDestroy()
        referViewModel.resetState()
    }



    fun showReferralDialog(
        fragment: Fragment,
        type: String,
        reason: String,
        benId: Long,
        cbacId: Long,
        cbacViewModel: CbacViewModel
    ) {
        val dialog = Dialog(fragment.requireContext(), R.style.FullScreenDialog)
        val binding = FragmentNewFormBinding.inflate(fragment.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)


        referViewModel.initFromArgs(
            benId = benId,
            referralReason = reason,
            cbacId = cbacId,
            referralType = type
        )


        binding.benId.text = benId.toString()

        referViewModel.benName.observe(fragment.viewLifecycleOwner) {
            binding.tvBenName.text = it
        }

        referViewModel.benAgeGender.observe(fragment.viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                referViewModel.updateListOnValueChanged(formId, index)
                binding.form.rvInputForm.adapter?.notifyDataSetChanged()



            }, isEnabled = true

        )

        binding.form.rvInputForm.adapter = adapter

        fragment.lifecycleScope.launch {
            fragment.repeatOnLifecycle(Lifecycle.State.STARTED) {
                referViewModel.formList.collect {
                    adapter.submitList(it)
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            referViewModel.saveForm()
        }

        referViewModel.state.observe(fragment.viewLifecycleOwner) {
            if (it == NCDReferDialogViewModel.State.SAVE_SUCCESS) {

                val referral = referViewModel.referalCache
                cbacViewModel.addReferral(referral)

                val referralType = CbacViewModel.ReferralType.valueOf(type)
                cbacViewModel.markReferralCompleted(referralType)

                Toast.makeText(
                    fragment.requireContext(),
                    fragment.getString(R.string.submit),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }




}