package sh.now.alecsisduart.who_says.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.confirm_dialog.*
import sh.now.alecsisduart.who_says.R

private const val ID = "confirmdialog.param.id"
private const val TITLE = "confirmdialog.param.title"
private const val MESSAGE = "confirmdialog.param.message"

class ConfirmDialog : DialogFragment() {

    private var dialogId: Int = 0
    private lateinit var title: String
    private lateinit var message: String
    private var isActive = true

    private lateinit var listener: ConfirmDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfirmDialogListener) {
            listener = context
        } else {
            throw  Error("${context::class.java.simpleName} must implement ConfirmDialogListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.confirm_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments!!.let {
            title = it.getString(TITLE, "NOT SPECIFIED")
            message = it.getString(MESSAGE, "")
            dialogId = it.getInt(ID)
        }

        dialog?.let { d: Dialog ->
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.window?.let { w: Window ->
                w.setBackgroundDrawableResource(R.color.colorTransparent)
            }
        }


        confirmTitle.text = title
        confirmContent.text = message

        okButton.setOnClickListener { onConfirm() }
        cancelButton.setOnClickListener { onCancel() }
    }

    fun isActive(): Boolean {
        return isActive
    }

    private fun onConfirm() {
        listener.onConfirmDialogConfirmClick(dialogId)
        this.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isActive = false
    }

    private fun onCancel() {
        listener.onConfirmDialogCancelClick(dialogId)
        this.dismiss()
    }

    interface ConfirmDialogListener {
        fun onConfirmDialogConfirmClick(id: Int)
        fun onConfirmDialogCancelClick(id: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(fragmentManager: FragmentManager, title: String, message: String, id: Int): ConfirmDialog {
            val confirmDialog = ConfirmDialog().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(MESSAGE, message)
                    putInt(ID, id)
                }
            }
            confirmDialog.show(fragmentManager, title.trim())
            return confirmDialog
        }
    }
}