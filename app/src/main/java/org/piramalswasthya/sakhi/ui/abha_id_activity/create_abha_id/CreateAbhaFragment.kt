package org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.work.Operation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentCreateAbhaBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id.CreateAbhaViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class CreateAbhaFragment : Fragment() {

    private lateinit var navController: NavController

    private var _binding: FragmentCreateAbhaBinding? = null

    private val binding: FragmentCreateAbhaBinding
        get() = _binding!!
    private val viewModel: CreateAbhaViewModel by viewModels()

    private val channelId = "download abha card"


    private var timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            binding.timerResendOtp.text = sec.toString()
        }

        // When the task is over it will print 00:00:00 there
        override fun onFinish() {
            binding.resendOtp.isEnabled = true
            binding.timerResendOtp.visibility = View.INVISIBLE
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back button press here
                Timber.d("handleOnBackPressed")
                exitAlert.show()
            }
        }
    }

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit")
            .setMessage("Do you want to go back?")
            .setPositiveButton("Yes") { _, _ ->
                activity?.finish()
            }
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private val beneficiaryDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle("beneficiary abha mapping.")
            .setMessage("linking abha to beneficiary")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateAbhaBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        navController = findNavController()


        val intent = requireActivity().intent

        val benId = intent.getLongExtra("benId", 0)
        val benRegId = intent.getLongExtra("benRegId", 0)

        viewModel.createHID(benId, benRegId)

        viewModel.showDisclaimer.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    beneficiaryDisclaimer.setMessage("mapping abha id to beneficiary $benId")
                    beneficiaryDisclaimer.show()
                }
            }
        }
        binding.tietAadhaarOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnVerifyOTP.isEnabled = p0 != null && p0.length == 6
            }
        })

        binding.btnVerifyOTP.setOnClickListener {
            viewModel.verifyOtp(binding.tietAadhaarOtp.text.toString())
        }

        binding.btnDownloadAbhaNo.setOnClickListener{
            binding.txtDownloadAbha.visibility = View.INVISIBLE
            binding.clDownloadAbha.visibility = View.INVISIBLE
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                State.IDLE -> {}
                State.LOADING -> {
                    binding.pbCai.visibility = View.VISIBLE
                    binding.clCreateAbhaId.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.ERROR_NETWORK -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clCreateAbhaId.visibility = View.INVISIBLE
                    binding.clError.visibility = View.VISIBLE
                }
                State.ERROR_SERVER -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.VISIBLE
                }
                State.ABHA_GENERATE_SUCCESS -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clCreateAbhaId.visibility = View.VISIBLE
                    binding.clVerifyMobileOtp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.OTP_GENERATE_SUCCESS -> {
                    binding.clVerifyMobileOtp.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                    startResendTimer()
                }
                State.OTP_VERIFY_SUCCESS -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clCreateAbhaId.visibility = View.VISIBLE
                    binding.clDownloadAbha.visibility = View.INVISIBLE
                    binding.clVerifyMobileOtp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.DOWNLOAD_SUCCESS -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clCreateAbhaId.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.DOWNLOAD_ERROR -> {
                    binding.pbCai.visibility = View.INVISIBLE
                    binding.clCreateAbhaId.visibility = View.VISIBLE
                    binding.clDownloadAbha.visibility = View.INVISIBLE
                    binding.clVerifyMobileOtp.visibility = View.VISIBLE
                    binding.tvErrorTextVerify.visibility = View.VISIBLE
                    startResendTimer()
                }
                State.ERROR_INTERNAL -> {}
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorTextVerify.text = it
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }

        viewModel.cardBase64.observe(viewLifecycleOwner) {
            it?.let {
                showFileNotification(it)
            }
        }
        binding.btnDownloadAbhaYes.setOnClickListener {
            viewModel.generateOtp()
            binding.clDownloadAbha.visibility = View.GONE
        }

        binding.resendOtp.setOnClickListener {
            viewModel.generateOtp()
            startResendTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
        _binding = null
    }

    private fun showFileNotification(fileStr: String) {
        val fileName =
            "${viewModel.hidResponse.value?.name}_${System.currentTimeMillis()}.png"
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,channelId,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)

//            val notification = NotificationCompat.Builder(requireContext(),channelId)
//                .setContentTitle("Downloading abha card")
//                .setContentText("Downloading")
//                .setSmallIcon(R.drawable.ic_download)
//                .setProgress(100, 0, true)
//                .build()
//            notificationManager.notify(0, notification)
        }
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)
//        val inputStream = fileStr.byteInputStream(Charset.defaultCharset())
//        val outputStream = FileOutputStream(file)
        val data: ByteArray = Base64.decode(fileStr,0)
        FileOutputStream(file).use { stream -> stream.write(data) }

//        val buffer = ByteArray(1024)
//        var bytesRead = inputStream.read(buffer)
//        var totalBytesRead = bytesRead.toLong()
//        while (bytesRead != -1) {
//            outputStream.write(buffer, 0, bytesRead)
//            totalBytesRead += bytesRead
//            bytesRead = inputStream.read(buffer)
//        }
//        outputStream.close()
//        inputStream.close()

        MediaScannerConnection.scanFile(
            requireContext(),
            arrayOf(file.toString()),
            null,
        ) { _, uri ->
            run {
                showDownload(fileName, uri, notificationManager)
            }
        }
    }

    private fun showDownload(fileName: String, uri: Uri, notificationManager: NotificationManager) {
        val notificationBuilder = NotificationCompat.Builder(requireContext(),fileName)
            .setSmallIcon(R.drawable.ic_download)
            .setChannelId(channelId)
            .setContentTitle(fileName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationBuilder.setContentTitle(fileName)
            .setContentText(fileName)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    requireContext(),
                    0,
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/png")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        notificationManager.notify(1, notificationBuilder.build())

    }

    private fun startResendTimer() {
        binding.resendOtp.isEnabled = false
        binding.timerResendOtp.visibility = View.VISIBLE
        timer.start()
    }

    fun notifydownload() {
        val fileName =
            "${viewModel.hidResponse.value?.name}_${System.currentTimeMillis()}.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId,channelId,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(requireContext(),channelId)
                .setContentTitle("Downloading abha card")
                .setContentText("Downloading")
                .setSmallIcon(R.drawable.ic_download)
                .setProgress(100, 0, true)
                .build()
            notificationManager.notify(0, notification)
        }

        val state: LiveData<Operation.State> = WorkerUtils
            .triggerDownloadCardWorker(requireContext(), fileName, viewModel.otpTxnID)

        state.observe(viewLifecycleOwner) {
            when(it) {
                is Operation.State.SUCCESS -> {
                    binding.clDownloadAbha.visibility = View.INVISIBLE
                    Snackbar.make(binding.root,
                        "Downloading $fileName ", Snackbar.LENGTH_SHORT).show()
                }
                is Operation.State.FAILURE -> {
                    Toast.makeText(context, "Failed to download , Please retry", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}