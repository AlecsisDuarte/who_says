package sh.now.alecsisduart.who_says.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.settings_dialog.*
import sh.now.alecsisduart.who_says.*
import sh.now.alecsisduart.who_says.enums.GameSpeed
import sh.now.alecsisduart.who_says.enums.GridSize
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import sh.now.alecsisduart.who_says.services.MusicPlayerService
import java.lang.ClassCastException


private const val TAG = "SettingsDialog"

class SettingsDialog : DialogFragment() {

    private lateinit var configurationHelper: ConfigurationHelper
    private lateinit var musicPlayerHelper: MusicPlayerHelper

    private var isActive: Boolean = true

    private var originalSoundOn = true
    private lateinit var originalGameSpeed: GameSpeed
    private lateinit var originalGridSize: GridSize

    private lateinit var listener: SettingsDialogListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurationHelper = ConfigurationHelper.getInstance(requireContext())
        musicPlayerHelper = MusicPlayerHelper.getInstance(requireContext())

        configurationHelper.let {
            originalGridSize = it.gridSize
            originalGameSpeed = it.gameSpeed
            originalSoundOn = it.soundOn
        }

        dialog?.let { d: Dialog ->
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.window?.let { w: Window ->
                w.setBackgroundDrawableResource(R.color.colorTransparent)
            }

        }

        loadGameSpeed()
        loadGridSize()
        loadSoundOnSwitch()

        saveButton.setOnClickListener { onSaveButtonClick() }
        soundSwitchText.setOnClickListener {
            soundSwitch.performClick()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SettingsDialogListener) {
            listener = context
        } else {
            throw ClassCastException("${context.packageName} must implement SettingsDialogListener's onSavedSettingsDialog")
        }
    }


    private fun loadGameSpeed() {
        when (configurationHelper.gameSpeed) {
            GameSpeed.FAST -> fastSpeedRadioButton
            GameSpeed.INSANE -> insaneSpeedRadioButton
            GameSpeed.NORMAL -> normalSpeedRadioButton
        }.isChecked = true

        fastSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
        insaneSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
        normalSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
    }

    private fun loadGridSize() {
        when (configurationHelper.gridSize) {
            GridSize.NORMAL -> normalSizeRadioButton
            GridSize.BIG -> bigSizeRadioButton
        }.isChecked = true

        normalSizeRadioButton.setOnClickListener { view: View -> onGridSizeRadioButtonClick(view) }
        bigSizeRadioButton.setOnClickListener { view: View -> onGridSizeRadioButtonClick(view) }
    }

    private fun loadSoundOnSwitch() {
        soundSwitch.isChecked = configurationHelper.soundOn
        soundSwitch.setOnClickListener { view: View -> onSoundOnSwitchClick(view) }
    }

    private fun onGridSizeRadioButtonClick(view: View) {
        val radioButton = view as RadioButton
        configurationHelper.gridSize = when (radioButton) {
            normalSizeRadioButton -> {
                musicPlayerHelper.lowSoundAsync()
                GridSize.NORMAL
            }
            bigSizeRadioButton -> {
                musicPlayerHelper.highSoundAsync()
                GridSize.BIG
            }
            else -> configurationHelper.gridSize
        }
    }

    private fun onGameSpeedRadioButtonClick(view: View) {
        val radioButton = view as RadioButton
        configurationHelper.gameSpeed = when (radioButton) {
            fastSpeedRadioButton -> {
                musicPlayerHelper.highSoundAsync()
                GameSpeed.FAST
            }
            insaneSpeedRadioButton -> {
                musicPlayerHelper.middleSoundAsync()
                GameSpeed.INSANE
            }
            normalSpeedRadioButton -> {
                musicPlayerHelper.lowSoundAsync()
                GameSpeed.NORMAL
            }
            else -> configurationHelper.gameSpeed
        }
    }

    private fun onSoundOnSwitchClick(view: View) {
        val switch = view as Switch
        if (switch.isChecked) {
            MusicPlayerService.playHighSound(requireContext())
        }

        configurationHelper.soundOn = switch.isChecked
    }

    private fun onSaveButtonClick() {
        musicPlayerHelper.buttonSoundAsync()
        configurationHelper.let {
            listener.onSavedSettingsDialog(
                gridSizeChanged = it.gridSize != originalGridSize,
                gameSpeedChanged = it.gameSpeed != originalGameSpeed,
                soundOnChanged = it.soundOn != originalSoundOn
            )
        }

        this.dismiss()
    }

    fun isActive(): Boolean {
        return isActive
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isActive = false
    }

    interface SettingsDialogListener {
        fun onSavedSettingsDialog(gridSizeChanged: Boolean, gameSpeedChanged: Boolean, soundOnChanged: Boolean)

    }

    companion object {
        @JvmStatic
        fun newInstance(
            fragmentManager: FragmentManager
        ): SettingsDialog {
            val sd = SettingsDialog()
            sd.showNow(fragmentManager, TAG)
            return sd
        }
    }
}