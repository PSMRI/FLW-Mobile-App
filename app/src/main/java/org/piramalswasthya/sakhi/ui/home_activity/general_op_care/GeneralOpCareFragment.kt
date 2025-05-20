package org.piramalswasthya.sakhi.ui.home_activity.general_op_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

class GeneralOpCareFragment : Fragment() {

    companion object {
        fun newInstance() = GeneralOpCareFragment()
    }

    private lateinit var viewModel: GeneralOpCareViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_op_care, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(GeneralOpCareViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__general_op,
                getString(R.string.icon_title_gop)
            )
        }
    }

}