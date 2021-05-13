package es.uniovi.asw.radarinen3b.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import es.uniovi.asw.radarinen3b.R
import es.uniovi.asw.radarinen3b.databinding.DialogLocationBinding


class SavedLocationDialogFragment(private val longitude: Int, private val latitude: Int) :
    DialogFragment() {
    private lateinit var binding: DialogLocationBinding
    internal lateinit var listener: SavedLocationDialogListener

    interface SavedLocationDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, uri: Uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as SavedLocationDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            binding = DialogLocationBinding.inflate(inflater)
            builder.setView(binding.root)
                // Add action buttons
                .setPositiveButton(R.string.saved_location_create
                ) { dialog, id ->
                    val uri = Uri.Builder()
                        .encodedPath("https://radarinen3bwebapp.herokuapp.com/#/uploadLocation")
                        .appendQueryParameter("title", binding.nameTxt.text.toString())
                        .appendQueryParameter("comment", binding.descriptionTxt.text.toString())
                        .appendQueryParameter("lat", latitude.toString())
                        .appendQueryParameter("long", longitude.toString()).build()
                    listener.onDialogPositiveClick(this, uri)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, id ->
                    getDialog()?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


}