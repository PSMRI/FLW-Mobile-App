package org.piramalswasthya.sakhi.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Adapter surface exposing dirty state so the unsaved-changes guard can be adapter-agnostic.
 */
interface DirtyState {
    /** Returns true if there are unsaved changes */
    val isDirty: LiveData<Boolean>

    /** Clear the dirty state (mark as no unsaved changes) */
    fun clearDirty()

    /** Mark the state as dirty (unsaved changes present) */
    fun markDirty()
}

/**
 * Optional: A simple implementation of DirtyState you can reuse for adapters or fragments.
 */
class SimpleDirtyState : DirtyState {
    private val _isDirty = MutableLiveData(false)
    override val isDirty: LiveData<Boolean> get() = _isDirty

    override fun clearDirty() {
        _isDirty.value = false
    }

    override fun markDirty() {
        _isDirty.value = true
    }
}
