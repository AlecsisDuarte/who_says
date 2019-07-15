package sh.now.alecsisduart.who_says.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.confirm_dialog.*
import sh.now.alecsisduart.who_says.R
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper


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

    private lateinit var mMusicPlayerHelper: MusicPlayerHelper

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

        mMusicPlayerHelper = MusicPlayerHelper.getInstance(requireContext())

        loadTitle()
        loadMessage()

        confirmButton.setButtonTextAndListener(confirmButtonText, confirmButtonOnClickListener)
        cancelButton.setButtonTextAndListener(cancelButtonText, cancelButtonOnClickListener)
    }

    private fun loadTitle() {
        //In case of no title we also hide the Title/Content divider
        if (title.isNullOrEmpty()) {
            confirmTitle.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            confirmTitle.text = title
        }
    }

    private fun loadMessage() {
        if (message.isNullOrEmpty()) {
            confirmContent.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            confirmContent.text = message
        }
    }

    private fun MaterialButton.setButtonTextAndListener(text: CharSequence?, listener: View.OnClickListener?) {
        if (text.isNullOrEmpty()) {
            visibility = View.GONE
        } else {
            this.text = text
            this.setOnClickListener {
                mMusicPlayerHelper.buttonSoundAsync()
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

        //Specify the dialog ID for reference
        fun setDialogId(dialogId: Int) = apply { this.dialogId = dialogId }

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