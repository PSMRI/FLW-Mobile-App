package org.piramalswasthya.sakhi.ui.home_activity.incentives
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IncentiveListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentIncentivesSubBinding
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.ui.dialogs.UploadSourceDialog
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.FileViewerDialog
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class IncentivesSubFragment : Fragment() {

    private var _binding: FragmentIncentivesSubBinding? = null
    private val binding: FragmentIncentivesSubBinding get() = _binding!!

    private val viewModel: IncentivesViewModel by viewModels()

    private lateinit var adapter: IncentiveListAdapter

    private var currentSelectedItem: IncentiveDomain? = null

    private var currentPhotoUri: Uri? = null

    companion object {
        private const val MAX_FILE_SIZE_MB = 5
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L
        private const val TARGET_SIZE_BYTES = 200 * 1024L
        private const val MAX_DIMENSION = 1280
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                lifecycleScope.launch {
                    handleCameraImage(uri)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                lifecycleScope.launch {
                    handleGallerySelection(intent)
                }
            }
        }
    }

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                lifecycleScope.launch {
                    handleDocumentSelection(intent)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIncentivesSubBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFragmentResultListener()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadState.collect { state ->
                    when (state) {
                        is IncentivesViewModel.UploadState.Loading -> {
                        }
                        is IncentivesViewModel.UploadState.Success -> {
                            Toast.makeText(requireContext(), "Submitted", Toast.LENGTH_SHORT).show()
                            WorkerUtils.triggerAmritPullWorker(requireContext())
                            viewModel.resetUploadState()
                            findNavController().navigateUp()
                        }
                        is IncentivesViewModel.UploadState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetUploadState()
                        }
                        is IncentivesViewModel.UploadState.Idle -> {  }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = IncentiveListAdapter(
            IncentiveListAdapter.FileClickListener(
                onUpload = { item ->
                    currentSelectedItem = item
                    showUploadSourceDialog()
                },
                onView = { item ->
                    showUploadedFiles(item)
                },
                onSubmit = { item ->
                    showSubmitConfirmationDialog(item)
                }
            )
        )

        binding.rvSubIncentive.adapter = adapter
        binding.rvSubIncentive.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
        )
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("records_key") { _, bundle ->
            val records = bundle.getParcelableArrayList<IncentiveDomain>("records") ?: emptyList()
            val activityName = bundle.getString("activityName") ?: "Unknown"

            binding.activityName = activityName
            adapter.submitList(records)
        }
    }

    private fun showUploadSourceDialog() {
        UploadSourceDialog.newInstance(
            onCameraSelected = { openCamera() },
            onGallerySelected = { openGallery() },
            onDocumentSelected = { openDocumentPicker() }
        ).show(childFragmentManager, "UploadSourceDialog")
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            cameraLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            Timber.e(e, "Error opening camera")
            Toast.makeText(
                requireContext(),
                "Unable to open camera",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "INCENTIVE_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        try {
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening gallery")
            Toast.makeText(
                requireContext(),
                "Unable to open gallery",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }

        try {
            documentPickerLauncher.launch(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening document picker")
            Toast.makeText(
                requireContext(),
                "Unable to open document picker",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun getFileSize(uri: Uri): Long {
        return try {
            requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize
            } ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "Error getting file size")
            0L
        }
    }


    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${(bytes / 1024.0).roundToInt()} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }


    private suspend fun compressImage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val originalFile = uriToFile(uri) ?: return@withContext null

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(originalFile.absolutePath, boundsOptions)

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight

            var inSampleSize = 1
            if (origWidth > MAX_DIMENSION || origHeight > MAX_DIMENSION) {
                val halfWidth = origWidth / 2
                val halfHeight = origHeight / 2
                while ((halfWidth / inSampleSize) >= MAX_DIMENSION &&
                    (halfHeight / inSampleSize) >= MAX_DIMENSION
                ) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            var bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, decodeOptions)
                ?: return@withContext null

            val currentWidth = bitmap.width
            val currentHeight = bitmap.height
            if (currentWidth > MAX_DIMENSION || currentHeight > MAX_DIMENSION) {
                val scale = MAX_DIMENSION.toFloat() / maxOf(currentWidth, currentHeight)
                val newWidth = (currentWidth * scale).toInt()
                val newHeight = (currentHeight * scale).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                if (scaled != bitmap) bitmap.recycle()
                bitmap = scaled
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(null)
            val compressedFile = File.createTempFile(
                "COMPRESSED_${timeStamp}_",
                ".jpg",
                storageDir
            )

            var quality = 80
            var compressedSize: Long

            do {
                FileOutputStream(compressedFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                compressedSize = compressedFile.length()
                quality -= 5
            } while (compressedSize > TARGET_SIZE_BYTES && quality > 10)

            bitmap.recycle()

            Timber.d("Image compressed: ${formatFileSize(originalFile.length())} -> ${formatFileSize(compressedSize)} (quality=$quality)")

            Uri.fromFile(compressedFile)
        } catch (e: Exception) {
            Timber.e(e, "Error compressing image")
            null
        }
    }


    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_", ".jpg", requireContext().cacheDir)
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Timber.e(e, "Error converting URI to File")
            null
        }
    }


    private suspend fun handleCameraImage(uri: Uri) {
        try {
            withContext(Dispatchers.Main) {
                binding.progressBar?.visibility = View.VISIBLE
            }

            val fileSize = getFileSize(uri)
            Timber.d("Camera image size: ${formatFileSize(fileSize)}")

            val finalUri = compressImage(uri) ?: run {
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Unable to compress image. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }

            withContext(Dispatchers.Main) {
                currentSelectedItem?.let { item ->
                    addFilesToIncentive(item, mutableListOf(finalUri))
                }
                binding.progressBar?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling camera image")
            withContext(Dispatchers.Main) {
                binding.progressBar?.visibility = View.GONE
                Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun handleGallerySelection(data: Intent) {
        val selectedFiles = mutableListOf<Uri>()

        data.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let { uri ->
                    selectedFiles.add(uri)
                }
            }
        } ?: data.data?.let { uri ->
            selectedFiles.add(uri)
        }

        if (selectedFiles.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "No images selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        withContext(Dispatchers.Main) {
            binding.progressBar?.visibility = View.VISIBLE
        }

        val validFiles = mutableListOf<Uri>()
        val oversizedFiles = mutableListOf<String>()

        for (uri in selectedFiles) {
            val compressedUri = compressImage(uri)
            if (compressedUri != null) {
                validFiles.add(compressedUri)
            } else {
                oversizedFiles.add("${uri.lastPathSegment ?: "Unknown"} (failed to compress)")
            }
        }

        withContext(Dispatchers.Main) {
            binding.progressBar?.visibility = View.GONE

            if (oversizedFiles.isNotEmpty()) {
                val message = buildString {
                    append("The following files exceed ${MAX_FILE_SIZE_MB}MB and were not added:\n\n")
                    oversizedFiles.forEach { append("• $it\n") }
                    if (validFiles.isNotEmpty()) {
                        append("\n${validFiles.size} valid file(s) were added.")
                    }
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }

            if (validFiles.isNotEmpty()) {
                currentSelectedItem?.let { item ->
                    addFilesToIncentive(item, validFiles)
                }
            }
        }
    }


    private suspend fun handleDocumentSelection(data: Intent) {
        val selectedFiles = mutableListOf<Uri>()

        data.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let { uri ->
                    selectedFiles.add(uri)
                }
            }
        } ?: data.data?.let { uri ->
            selectedFiles.add(uri)
        }

        if (selectedFiles.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "No documents selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        withContext(Dispatchers.Main) {
            binding.progressBar?.visibility = View.VISIBLE
        }

        val validFiles = mutableListOf<Uri>()
        val oversizedFiles = mutableListOf<String>()

        for (uri in selectedFiles) {
            val fileSize = getFileSize(uri)
            val fileName = uri.lastPathSegment ?: "Unknown file"

            if (fileSize > MAX_FILE_SIZE_BYTES) {
                oversizedFiles.add("$fileName (${formatFileSize(fileSize)})")
            } else {
                validFiles.add(uri)
                try {
                    withContext(Dispatchers.Main) {
                        requireContext().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error taking persistable permission for URI: $uri")
                }
            }
        }

        withContext(Dispatchers.Main) {
            binding.progressBar?.visibility = View.GONE

            if (oversizedFiles.isNotEmpty()) {
                val message = buildString {
                    append("The following files exceed ${MAX_FILE_SIZE_MB}MB and were not added:\n\n")
                    oversizedFiles.forEach { append("• $it\n") }
                    if (validFiles.isNotEmpty()) {
                        append("\n${validFiles.size} valid file(s) were added.")
                    }
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }

            if (validFiles.isNotEmpty()) {
                currentSelectedItem?.let { item ->
                    addFilesToIncentive(item, validFiles)
                }
            }
        }
    }

    private fun addFilesToIncentive(item: IncentiveDomain, files: List<Uri>) {
        val fileUris = files.map { it.toString() }
        val updatedFiles = item.uploadedFiles.toMutableList()
        updatedFiles.addAll(fileUris)

        item.uploadedFiles = updatedFiles
        item.fileCount = updatedFiles.size
        item.isSubmitted = false

        val position = adapter.currentList.indexOf(item)
        if (position >= 0) {
            adapter.notifyItemChanged(position)
        }

        Toast.makeText(
            requireContext(),
            "${files.size} file(s) added. Total: ${item.fileCount} files",
            Toast.LENGTH_SHORT
        ).show()

        Timber.d("Files added for item ${item.record.id}: ${fileUris.joinToString()}")
    }

    private fun showUploadedFiles(item: IncentiveDomain) {
        if (item.fileCount == 0) {
            Toast.makeText(
                requireContext(),
                "No files uploaded for this item",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        FileViewerDialog.newInstance(
            item = item,
            onFileDeleted = { deletedFileUri ->
                handleFileDeleted(item, deletedFileUri)
            }
        ).show(childFragmentManager, "FileViewerDialog")
    }

    private fun handleFileDeleted(item: IncentiveDomain, fileUri: String) {
        val updatedFiles = item.uploadedFiles.toMutableList()
        updatedFiles.remove(fileUri)

        item.uploadedFiles = updatedFiles
        item.fileCount = updatedFiles.size
        item.isSubmitted = false

        val position = adapter.currentList.indexOf(item)
        if (position >= 0) {
            adapter.notifyItemChanged(position)
        }

        // viewModel.updateIncentiveFiles(item)

        Toast.makeText(
            requireContext(),
            "File deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSubmitConfirmationDialog(item: IncentiveDomain) {
        if (item.fileCount == 0) {
            Toast.makeText(
                requireContext(),
                "Please upload files before submitting",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (item.isSubmitted) {
            Toast.makeText(
                requireContext(),
                "Already submitted",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Submit Documents")
            .setMessage("Are you sure you want to submit ${item.fileCount} file(s) for ${item.record.name}?\n\nThis action cannot be undone.")
            .setPositiveButton("Submit") { dialog, _ ->
                submitDocumentsToServer(item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun submitDocumentsToServer(item: IncentiveDomain) {
        binding.progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = viewModel.uploadIncentiveDocuments(item)

                kotlinx.coroutines.delay(2000)

                item.isSubmitted = true
                item.submittedAt = System.currentTimeMillis()

                val position = adapter.currentList.indexOf(item)
                if (position >= 0) {
                    adapter.notifyItemChanged(position)
                }

                // viewModel.updateIncentiveStatus(item)

                binding.progressBar?.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Documents submitted successfully!",
                    Toast.LENGTH_LONG
                ).show()

                Timber.d("Documents submitted for item ${item.record.id}")

            } catch (e: Exception) {
                Timber.e(e, "Error submitting documents")
                binding.progressBar?.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Failed to submit documents. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}