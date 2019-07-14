package sh.now.alecsisduart.who_says.dialogs

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.resources.MaterialAttributes
import kotlinx.android.synthetic.main.confirm_dialog.*
import sh.now.alecsisduart.who_says.R

//
//private const val ID = "confirmdialog.param.id"
//private const val TITLE = "confirmdialog.param.title"
//private const val MESSAGE = "confirmdialog.param.message"
//private const val CONFIRM_BUTTON = "confirmdialog.param.confirm_button"
//private const val CANCEL_BUTTON_TEXT = "confirmdialog.param.cancel_button"

private const val TAG = "confirmDialog"

class ConfirmDialog private constructor() : DialogFragment() {

    internal var dialogId: Int = 0
    internal var title: CharSequence? = null
    internal var message: CharSequence? = null

    internal var confirmButtonOnClickListener: View.OnClickListener? = null
    internal var cancelButtonOnClickListener: View.OnClickListener? = null

    internal var confirmButtonText: CharSequence? = null
    internal var cancelButtonText: CharSequence? = null

    private var isActive = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.confirm_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.let { w: Window ->
                w.setBackgroundDrawableResource(R.color.colorTransparent)
            }
        }


        if (title.isNullOrEmpty()) {
            confirmTitle.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            confirmTitle.text = title
        }

        if (message.isNullOrEmpty()) {
            confirmContent.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            confirmContent.text = message
        }

        if (cancelButtonText.isNullOrEmpty()) {
            cancelButton.visibility = View.GONE
        } else {
            cancelButton.let { mb ->
                mb.text = cancelButtonText
                mb.setOnClickListener {
                    this.dismiss()
                    cancelButtonOnClickListener?.onClick(it)
                }
            }
        }
        confirmButton.setButtonTextAndListener(confirmButtonText, confirmButtonOnClickListener)
        cancelButton.setButtonTextAndListener(cancelButtonText, cancelButtonOnClickListener)
    }

    private inline fun MaterialButton.setButtonTextAndListener(text: CharSequence?, listener: View.OnClickListener?) {
        if (text.isNullOrEmpty()) {
            visibility = View.GONE
        } else {
            setText(text)
            setOnClickListener {
                close()
                listener?.onClick(it)
            }
        }
    }

    private fun close() {
        this.dismiss()
    }

    fun isActive(): Boolean {
        return isActive
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        cancelButtonOnClickListener?.onClick(view)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isActive = false
    }

    data class Builder(private var context: Context, private var fragmentManager: FragmentManager) {
        private var dialog = ConfirmDialog()
        private var dialogId: Int = 0
        private var title: CharSequence? = null
        private var message: CharSequence? = null

        private var confirmButtonOnClickListener: View.OnClickListener? = null
        private var cancelButtonOnClickListener: View.OnClickListener? = null

        private var confirmButtonText: CharSequence? = null
        private var cancelButtonText: CharSequence? = null

        private fun defaultMaterialButton(): MaterialButton {
            val mb = MaterialButton(context, null, R.attr.borderlessButtonStyle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mb.setTextAppearance(R.style.ContentText)
            } else {
                mb.setTextAppearance(context, R.style.ContentText)
            }

            return mb
        }

        fun setTitle(title: CharSequence) = apply { this.title = title }
        fun setMessage(message: String) = apply { this.message = message }

        fun setConfirmButton(resId: Int, listener: View.OnClickListener? = null) = apply {
            val text = context.getText(resId)
            this.setConfirmButton(text, listener)
        }

        fun setConfirmButton(text: CharSequence, listener: View.OnClickListener? = null) = apply {
            confirmButtonText = text
            confirmButtonOnClickListener = listener
        }

        fun setCancelButton(resId: Int, listener: View.OnClickListener? = null) = apply {
            val text = context.getText(resId)
            this.setCancelButton(text, listener)
        }

        fun setCancelButton(text: CharSequence, listener: View.OnClickListener? = null) = apply {
            cancelButtonText = text
            cancelButtonOnClickListener = listener
        }

        fun setDialogId(id: Int) = apply { this.dialogId = dialogId }

        fun show(): ConfirmDialog {
            dialog.let {
                it.title = this.title
                it.message = this.message
                it.dialogId = this.dialogId

                it.cancelButtonOnClickListener = this.cancelButtonOnClickListener
                it.cancelButtonText = this.cancelButtonText

                it.confirmButtonOnClickListener = this.confirmButtonOnClickListener
                it.confirmButtonText = this.confirmButtonText
            }

            dialog.show(fragmentManager, TAG)
            return dialog

        }
    }
}