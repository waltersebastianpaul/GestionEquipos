package com.example.gestionequipos.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.gestionequipos.R

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.fragment_progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return progressDialog
    }

    companion object {
        const val TAG = "ProgressDialogFragment"

        fun show(fragmentManager: FragmentManager): ProgressDialogFragment {
            val progressDialogFragment = ProgressDialogFragment()
            progressDialogFragment.show(fragmentManager, TAG)
            return progressDialogFragment
        }

        fun dismiss(fragmentManager: FragmentManager) {
            val progressDialogFragment = fragmentManager.findFragmentByTag(TAG) as ProgressDialogFragment?
            progressDialogFragment?.dismiss()
        }
    }
}
