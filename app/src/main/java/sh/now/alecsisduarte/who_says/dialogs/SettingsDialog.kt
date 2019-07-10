package sh.now.alecsisduarte.who_says.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.pause_dialog.*
import kotlinx.android.synthetic.main.settings_dialog.*
import sh.now.alecsisduarte.who_says.*
import sh.now.alecsisduarte.who_says.dialogs.SettingsDialog.SettingsDialogListener
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.services.MusicPlayerService
import java.lang.ClassCastException

private const val GRID_SIZE = "sh.now.alecsisduarte.who_says.settings.grid_size"
private const val GAME_SPEED = "sh.now.alecsisduarte.who_says.settings.game_speed"
private const val SOUND_ON = "sh.now.alecsisduarte.who_says.settings.sound_on"

class SettingsDialog : DialogFragment() {
    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var soundOn: Boolean = false
    private var isActive: Boolean = true

    private lateinit var listener: SettingsDialogListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments!!.let {
            gridSize = GridSize.valueOf(it.getString(GRID_SIZE, GridSize.NORMAL.name))
            gameSpeed = GameSpeed.valueOf(it.getString(GAME_SPEED, GameSpeed.FAST.name))
            soundOn = it.getBoolean(SOUND_ON)
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

        saveButton.setOnClickListener { view: View -> onSaveButtonClick(view) }
        soundSwitchText.setOnClickListener { view: View ->
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
        when (gameSpeed) {
            GameSpeed.FAST -> fastSpeedRadioButton
            GameSpeed.INSANE -> insaneSpeedRadioButton
            GameSpeed.NORMAL -> normalSpeedRadioButton
        }.isChecked = true

        fastSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
        insaneSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
        normalSpeedRadioButton.setOnClickListener { view: View -> onGameSpeedRadioButtonClick(view) }
    }

    private fun loadGridSize() {
        when (gridSize) {
            GridSize.NORMAL -> normalSizeRadioButton
            GridSize.BIG -> bigSizeRadioButton
        }.isChecked = true

        normalSizeRadioButton.setOnClickListener { view: View -> onGridSizeRadioButtonClick(view) }
        bigSizeRadioButton.setOnClickListener { view: View -> onGridSizeRadioButtonClick(view) }
    }

    private fun loadSoundOnSwitch() {
        soundSwitch.isChecked = soundOn
        soundSwitch.setOnClickListener { view: View -> onSoundOnSwitchClick(view) }
    }

    private fun onGridSizeRadioButtonClick(view: View) {
        val radioButton = view as RadioButton
        gridSize = when (radioButton) {
            normalSizeRadioButton -> {
                makeLowSound()
                GridSize.NORMAL
            }
            bigSizeRadioButton -> {
                makeHighSound()
                GridSize.BIG
            }
            else -> gridSize
        }
    }

    private fun onGameSpeedRadioButtonClick(view: View) {
        val radioButton = view as RadioButton
        gameSpeed = when (radioButton) {
            fastSpeedRadioButton -> {
                makeHighSound()
                GameSpeed.FAST
            }
            insaneSpeedRadioButton -> {
                makeMiddleSound()
                GameSpeed.INSANE
            }
            normalSpeedRadioButton -> {
                makeLowSound()
                GameSpeed.NORMAL
            }
            else -> gameSpeed
        }
    }

    private fun onSoundOnSwitchClick(view: View) {
        val switch = view as Switch
        if (switch.isChecked) {
            MusicPlayerService.playHighSound(requireContext())
        }

        soundOn = switch.isChecked
    }

    private fun onSaveButtonClick(view: View) {
        makeButtonSound()
        listener.onSavedSettingsDialog(gridSize, gameSpeed, soundOn)
        this.dismiss()
    }

    fun isActive(): Boolean {
        return isActive
    }

    private fun makeButtonSound() {
        if (soundOn) {
            MusicPlayerService.playButtonSound(requireContext())
        }
    }

    private fun makeLowSound() {
        if (soundOn) {
            MusicPlayerService.playLowSound(requireContext())
        }
    }

    private fun makeHighSound() {
        if (soundOn) {
            MusicPlayerService.playHighSound(requireContext())
        }
    }

    private fun makeMiddleSound() {
        if (soundOn) {
            MusicPlayerService.playMiddleSound(requireContext())
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isActive = false
    }

    interface SettingsDialogListener {
        fun onSavedSettingsDialog(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean)

    }

    companion object {
        @JvmStatic
        fun newInstance(
            fragmentManager: FragmentManager,
            gridSize: GridSize,
            gameSpeed: GameSpeed,
            soundOn: Boolean
        ): SettingsDialog {
            val sd = SettingsDialog().apply {
                arguments = Bundle().apply {
                    putString(GRID_SIZE, gridSize.name)
                    putString(GAME_SPEED, gameSpeed.name)
                    putBoolean(SOUND_ON, soundOn)
                }
            }
            sd.showNow(fragmentManager, SettingsDialog::class.java.simpleName)
            return sd


        }
    }
}