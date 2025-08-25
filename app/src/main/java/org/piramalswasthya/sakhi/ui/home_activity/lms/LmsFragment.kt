package org.piramalswasthya.sakhi.ui.home_activity.lms

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class LmsFragment: Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }
    private val viewModel: LmsViewModel by viewModels()

    companion object {
        fun newInstance() = LmsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLMS()
    }

    private fun setupLMS() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                Timber.tag("LMSActionId").d(it.actionId.toString())
                if(it.actionId.toString().contains("2131361960"))
                {
                    val pdfFile = copyPdfToCache(requireContext(), "dummy.pdf", R.raw.dummy)
                    openPdfFile(requireContext(), pdfFile)
                }
                else{
                    findNavController().navigate(R.id.videoTutorialFragmet)
                }
            },
            viewModel.scope
        )
        binding.rvIconGrid.adapter = rvAdapter
        rvAdapter.submitList(iconDataset.getLmsDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic_help,
                getString(R.string.help_support)
            )
        }
    }

    private fun copyPdfToCache(context: Context, fileName: String, rawResourceId: Int): File {
        val cacheFile = File(context.cacheDir, fileName)

        if (!cacheFile.exists()) {
            try {
                val inputStream = context.resources.openRawResource(rawResourceId)
                val outputStream = FileOutputStream(cacheFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return cacheFile
    }

    private fun openPdfFile(context: Context, file: File) {
        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Must match the authority in manifest
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }


}