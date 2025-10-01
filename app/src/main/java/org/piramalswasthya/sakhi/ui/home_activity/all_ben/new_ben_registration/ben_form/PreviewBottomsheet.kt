package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import org.piramalswasthya.sakhi.model.PreviewItem


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.PreviewAdapter

class PreviewBottomSheet : BottomSheetDialogFragment() {

    private var previewList: List<PreviewItem> = emptyList()
    private var onSubmit: (() -> Unit)? = null
    private var onEdit: (() -> Unit)? = null

    fun setData(list: List<PreviewItem>) {
        previewList = list
    }

    fun setCallbacks(onEdit: (() -> Unit)? = null, onSubmit: (() -> Unit)? = null) {
        this.onEdit = onEdit
        this.onSubmit = onSubmit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.bottomsheet_preview, container, false)
        val rv = v.findViewById<RecyclerView>(R.id.recyclerPreview)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = PreviewAdapter(previewList) // Here you can pass Glide loader in prod

        v.findViewById<Button>(R.id.btnEditPreview).setOnClickListener {
            dismiss()
            onEdit?.invoke()
        }

        v.findViewById<Button>(R.id.btnSubmitPreview).setOnClickListener {
            dismiss()
            onSubmit?.invoke()
        }
        return v
    }
}
