package org.piramalswasthya.sakhi.utils


import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.DialogFileViewerBinding
import org.piramalswasthya.sakhi.databinding.ItemFileBinding
import org.piramalswasthya.sakhi.model.IncentiveDomain
import timber.log.Timber

class FileViewerDialog : DialogFragment() {

    private var _binding: DialogFileViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var item: IncentiveDomain
    private var onFileDeleted: ((String) -> Unit)? = null

    companion object {
        private const val ARG_ITEM = "arg_item"

        fun newInstance(
            item: IncentiveDomain,
            onFileDeleted: (String) -> Unit
        ): FileViewerDialog {
            return FileViewerDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                }
                this.onFileDeleted = onFileDeleted
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable(ARG_ITEM) ?: throw IllegalArgumentException("Item required")
        }
        setStyle(STYLE_NORMAL, androidx.appcompat.R.style.Animation_AppCompat_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFileViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Files for ${item.record.name}"
        binding.tvFileCount.text = "${item.fileCount} file(s)"

        // Show submission status
      /*  if (item.isSubmitted) {
            binding.tvSubmittedStatus.visibility = View.VISIBLE
            binding.tvSubmittedStatus.text = "Submitted - Files cannot be deleted"
        } else {
            binding.tvSubmittedStatus.visibility = View.GONE
        }*/

        setupRecyclerView()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        val adapter = FileListAdapter(
            files = item.uploadedFiles,
            isSubmitted = item.isSubmitted,
            onFileClick = { fileUri ->
                openFile(fileUri)
            },
            onDeleteClick = { fileUri ->
                showDeleteConfirmation(fileUri)
            }
        )

        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun openFile(fileUri: String) {
        try {
            val uri = Uri.parse(fileUri)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, requireContext().contentResolver.getType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: Exception) {
            Timber.e(e, "Error opening file: $fileUri")
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Unable to open file",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmation(fileUri: String) {
        if (item.isSubmitted) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Cannot delete files after submission",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteFile(fileUri)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteFile(fileUri: String) {
        onFileDeleted?.invoke(fileUri)

        // Refresh the list
        binding.tvFileCount.text = "${item.fileCount} file(s)"
        setupRecyclerView()

        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "File deleted",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()

        // Close dialog if no files left
        if (item.fileCount == 0) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for file list
    private class FileListAdapter(
        private val files: List<String>,
        private val isSubmitted: Boolean,
        private val onFileClick: (String) -> Unit,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
            val binding = ItemFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return FileViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            holder.bind(files[position], isSubmitted, onFileClick, onDeleteClick)
        }

        override fun getItemCount() = files.size

        class FileViewHolder(private val binding: ItemFileBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(
                fileUri: String,
                isSubmitted: Boolean,
                onFileClick: (String) -> Unit,
                onDeleteClick: (String) -> Unit
            ) {
                // Extract filename from URI
                val fileName = Uri.parse(fileUri).lastPathSegment ?: "Unknown file"
                binding.tvFileName.text = fileName

                // Set file icon based on type
                val fileType = getFileType(fileName)
                binding.ivFileIcon.setImageResource(getFileIcon(fileType))

                // Click to open file
                binding.root.setOnClickListener {
                    onFileClick(fileUri)
                }

                // Delete button
                if (isSubmitted) {
                    binding.btnDelete.visibility = View.GONE
                } else {
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.btnDelete.setOnClickListener {
                        onDeleteClick(fileUri)
                    }
                }
            }

            private fun getFileType(fileName: String): String {
                return fileName.substringAfterLast('.', "")
            }

            private fun getFileIcon(extension: String): Int {
                return when (extension.lowercase()) {
                    "pdf" -> R.drawable.ic_doc_upload
                    "jpg", "jpeg", "png", "gif", "webp" -> R.drawable.ic_doc_upload
                    "doc", "docx" -> R.drawable.ic_doc_upload
                    "xls", "xlsx" -> R.drawable.ic_doc_upload
                    else -> R.drawable.ic_doc_upload
                }
            }
        }
    }
}
