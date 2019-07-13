package sh.now.alecsisduart.who_says.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.pause_dialog.*
import sh.now.alecsisduart.who_says.R
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import java.lang.ClassCastException

private const val TAG = "PauseDialog"

class PauseDialog : DialogFragment() {
    private lateinit var configurationHelper: ConfigurationHelper
    private lateinit var musicPlayerHelper: MusicPlayerHelper

    private lateinit var listener: PauseDialogListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pause_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.let { d: Dialog ->
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.window?.let { w: Window ->
                w.setBackgroundDrawableResource(R.color.colorTransparent)
            }
            d.setOnCancelListener { onPlayButtonClick() }

        }
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        val context = requireContext()
        configurationHelper = ConfigurationHelper.getInstance(context)
        musicPlayerHelper = MusicPlayerHelper.getInstance(context)


        homeButton.setOnClickListener { onHomeButtonClick() }
        settingsButton.setOnClickListener { onSettingsButtonClick() }
        continueButton.setOnClickListener { onPlayButtonClick() }
        restartButton.setOnClickListener { onRestartButtonClick() }

    }

    private fun onHomeButtonClick() {
        musicPlayerHelper.buttonSoundAsync()
        listener.onPauseDialogListenerReturnHomeClick()
    }

    private fun onSettingsButtonClick() {
        musicPlayerHelper.buttonSoundAsync()
        val fm = requireFragmentManager()
        SettingsDialog.newInstance(fm)
    }

    private fun onPlayButtonClick() {
        musicPlayerHelper.buttonSoundAsync()
        listener.onPauseDialogListenerResumeButtonClick()
        this.dismiss()
    }

    private fun onRestartButtonClick() {
        musicPlayerHelper.buttonSoundAsync()
        listener.onPauseDialogListenerRestartButtonClick()
        this.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PauseDialogListener) {
            listener = context
        } else {
            throw ClassCastException("${context.packageName} must implement SettingsDialogListener's onSaveSettingDialog")
        }
    }


    interface PauseDialogListener : SettingsDialog.SettingsDialogListener {
        fun onPauseDialogListenerResumeButtonClick()
        fun onPauseDialogListenerReturnHomeClick()
        fun onPauseDialogListenerRestartButtonClick()
    }

    companion object {
        @JvmStatic
        fun newInstance(fragmentManager: FragmentManager): PauseDialog {
            val pauseDialog = PauseDialog()
            pauseDialog.show(fragmentManager, TAG)
            return pauseDialog
        }
    }
}