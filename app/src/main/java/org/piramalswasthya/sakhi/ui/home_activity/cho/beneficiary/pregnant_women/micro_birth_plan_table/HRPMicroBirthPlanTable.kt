package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.micro_birth_plan_table

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentHRPMicroBirthPlanTableBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.micro_birth_plan.HRPMicroBirthPlanViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date


@AndroidEntryPoint
class HRPMicroBirthPlanTable : Fragment() {


    private var _binding: FragmentHRPMicroBirthPlanTableBinding? = null

    private val binding: FragmentHRPMicroBirthPlanTableBinding
        get() = _binding!!

    private val viewModel: HRPMicroBirthPlanViewModel by viewModels()
    var lastMenstrualPeriodString=""
    var marriageDateString=""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHRPMicroBirthPlanTableBinding.inflate(inflater, container, false)
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnShare.setOnClickListener {
            val bitmap = getScreenShotFromView(binding.tableLayout)
            if (bitmap != null) {
                saveMediaToStorage(bitmap)
            }

        }

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.namePw.text = "${resources.getString(R.string.pw_name)} $it"
        }


        viewModel.currentLocation?.let {
            binding.scHwc.text =
                "${resources.getString(R.string.sc_hwc_tg_hosp)} :\n${it.village.name}"

        }

        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.age.text = "${resources.getString(R.string.b_age)} $it"
        }

        viewModel.recordExists.observe(viewLifecycleOwner) {
            lifecycleScope.launch {

                if (viewModel._microBirthPlanCache != null) {

                    binding.contactNo.text =
                        "${resources.getString(R.string.c_no)} ${viewModel._microBirthPlanCache.contactNumber1 ?: ""}\n2 - ${viewModel._microBirthPlanCache.contactNumber2 ?: ""}"
                    binding.husbandName.text =
                        "${resources.getString(R.string.husband_s_name)} :\n${viewModel.benDetails.spouseName ?: ""}"


                    val lastMenstrualPeriodLong = viewModel.benDetails.lastMenstrualPeriod
                    if(viewModel.benDetails.lastMenstrualPeriod!=null){
                         lastMenstrualPeriodString =
                            DateFormat.format("dd/MM/yyyy", lastMenstrualPeriodLong?.let { it1 -> Date(it1) })
                                .toString()
                    }

                    if (viewModel._hRPPregnantAssessCache != null) {
                        if (viewModel._hRPPregnantAssessCache?.lmpDate != null) {
                            val lmpDateLong = viewModel._hRPPregnantAssessCache?.lmpDate
                            val lmpDateString: String =
                                DateFormat.format("dd/MM/yyyy", lmpDateLong?.let { it1 -> Date(it1) })
                                    .toString()
                            binding.lmp.text =
                                "${resources.getString(R.string.lmp)} :\n${lmpDateString ?: ""}"
                            binding.tvLmpDate.text =
                                "${resources.getString(R.string.lmp)}:${lmpDateString ?: ""}"
                        } else {
                            binding.lmp.text =
                                "${resources.getString(R.string.lmp)} :\n${lastMenstrualPeriodString?: ""}"
                            binding.tvLmpDate.text = "${resources.getString(R.string.lmp)}:${lastMenstrualPeriodString ?: ""}"
                        }
                    }else{
                        binding.lmp.text =
                            "${resources.getString(R.string.lmp)} :\n${lastMenstrualPeriodString ?: ""}"
                        binding.tvLmpDate.text = "${resources.getString(R.string.lmp)}:${lastMenstrualPeriodString  ?: ""}"
                    }


//                    binding.lmp.text =
//                        "${resources.getString(R.string.lmp)} :\n${viewModel.benDetails.lastMenstrualPeriod ?: ""}"
                    binding.nrScHwc.text =
                        "${resources.getString(R.string.nearest_sc_hwc)} :\n${viewModel._microBirthPlanCache.nearestSc ?: ""}"
                    binding.block.text =
                        "${resources.getString(R.string.block)} :\n${viewModel._microBirthPlanCache.block ?: ""}"

                    if(viewModel.benDetails.marriageDate!=null){
                        val marriageDateLong = viewModel.benDetails.marriageDate
                         marriageDateString =
                            DateFormat.format("dd/MM/yyyy", marriageDateLong?.let { it1 -> Date(it1) })
                                .toString()
                    }


                    if (viewModel._hRPPregnantAssessCache != null) {
                        if (viewModel._hRPPregnantAssessCache?.edd != null) {
                            val eddDateLong = viewModel._hRPPregnantAssessCache?.edd
                            val eddDateString: String =
                                DateFormat.format("dd/MM/yyyy", eddDateLong?.let { it1 -> Date(it1) })
                                    .toString()
                            binding.edd.text =
                                "${resources.getString(R.string.edd)} :\n${eddDateString ?: ""}"
                            binding.tvEddDate.text =
                                "${resources.getString(R.string.edd)}:${eddDateString ?: ""}"
                        } else {

                            binding.edd.text =
                                "${resources.getString(R.string.edd)} :\n${marriageDateString ?: ""}"
                            binding.tvEddDate.text =
                                "${resources.getString(R.string.edd)} :\n${marriageDateString ?: ""}"
                        }
                    }else{
                        binding.tvEddDate.text =
                            "${resources.getString(R.string.edd)} :\n${marriageDateString ?: ""}"
                        binding.edd.text =
                            "${resources.getString(R.string.edd)} :\n${marriageDateString?: ""}"
                    }



                    binding.nrPhc.text =
                        "${resources.getString(R.string.nearest_24x7_phc)} :\n${viewModel._microBirthPlanCache.nearestPhc ?: ""}"
                    binding.bankAccNo.text =
                        "${resources.getString(R.string.bank_acc_no)} :\n${viewModel._microBirthPlanCache.bankac ?: ""}"
                    binding.nrFru.text =
                        "${resources.getString(R.string.nearest_fru)} :\n${viewModel._microBirthPlanCache.nearestFru ?: ""}"
                    binding.nrUsg.text =
                        "${resources.getString(R.string.nearest_usg_centre)} :\n${viewModel._microBirthPlanCache.usg ?: ""}"
                    binding.bloodGrp.text =
                        "${resources.getString(R.string.blood_group)} :\n${viewModel._microBirthPlanCache.bloodGroup ?: ""}"
                    binding.bloodDonr.text =
                        "${resources.getString(R.string.b_blood_donor)} ${viewModel._microBirthPlanCache.bloodDonors1 ?: ""}\n2 - " + "${viewModel._microBirthPlanCache.bloodDonors2 ?: ""}"
                    binding.birthComp.text =
                        "${resources.getString(R.string.birth_companion)} :\n${viewModel._microBirthPlanCache.birthCompanion ?: ""}"
                    binding.prsonTkCare.text =
                        "${resources.getString(R.string.person_who_will_take_care_of_children_if_any_when_the_pw_is_admitted_for_delivery)} :\n${viewModel._microBirthPlanCache.careTaker ?: ""}"
                    binding.nameOfContNo.text =
                        "${resources.getString(R.string.name_of_vhsnd_community_member_for_support_during_emergency)} :\n${viewModel._microBirthPlanCache.communityMember ?: ""}"
                    binding.modeOfTrasp.text =
                        "${resources.getString(R.string.mode_of_transportation_in_case_of_labour_pain)} :\n${viewModel._microBirthPlanCache.modeOfTransportation ?: ""}"

                }

            }
        }


    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__pregnancy,
                getString(R.string.micro_birth_plan)
            )
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        var screenshot: Bitmap? = null
        try {
            screenshot =
                Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {

        }
        return screenshot
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                shareImage(imageUri!!)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            var uriFile = Uri.fromFile(image)
            shareImage(uriFile)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(activity, "Captured View and saved to Gallery", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun shareImage(imageUri: Uri) {
        if (isPackageExist(requireContext(), "com.whatsapp") || isPackageExist(requireContext(), "com.whatsapp.w4b")) {
            val captionText = " ${getString(R.string.micro_birth_plan)} " +
                    "\n ASHA Name : ${viewModel.currentUser!!.name} " +
                    "\n Sub-center : ${viewModel.currentLocation!!.village.name}"
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
            intent.putExtra(Intent.EXTRA_TEXT, captionText)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
            intent.setPackage("com.whatsapp")
            intent.setType("image/png")
            startActivity(intent)

        } else {
            Toast.makeText(
                requireContext(),
                "Whats App is not installed in phone!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isPackageExist(context: Context, target: String): Boolean {
        return context.packageManager.getInstalledApplications(0)
            .find { info -> info.packageName == target } != null
    }


}