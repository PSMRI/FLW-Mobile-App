package org.piramalswasthya.sakhi.ui.home_activity.lms.userGuide

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.lms.LmsFragment

class UserGuideFragment : Fragment() {
        private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }
    private val viewModel: UserGuideViewModel by viewModels()



    companion object {
        fun newInstance() = UserGuideFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Use the ViewModel
        setupPDF()
    }

//    private fun displayPdf(pdfUri: Uri) {
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(
//                pdfUri,
//                "application/pdf"
//            )
//            addFlags (Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        startActivity (Intent.createChooser(intent, "Open PDF with"))
//    }
    private fun setupPDF() {



        InputType.IMAGE_VIEW
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                Toast.makeText(context,it.toString(), Toast.LENGTH_SHORT).show()
//                findNavController().navigate(it)
            },
            viewModel.scope
        )
        binding.rvIconGrid.adapter = rvAdapter
//        rvAdapter.submitList(iconDataset.getLmsDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic_help,
                getString(R.string.user_guide)
            )
        }
    }

}