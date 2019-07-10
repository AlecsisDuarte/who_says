package sh.now.alecsisduarte.who_says.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.pause_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import sh.now.alecsisduarte.who_says.R
import sh.now.alecsisduarte.who_says.RETURN_HOME_DIALOG_ID
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.services.MusicPlayerService
import java.lang.ClassCastException

private const val GRID_SIZE = "sh.now.alecsisduarte.who_says.settings.grid_size"
private const val GAME_SPEED = "sh.now.alecsisduarte.who_says.settings.game_speed"
private const val SOUND_ON = "sh.now.alecsisduarte.who_says.settings.sound_on"

class PauseDialog : DialogFragment() {

    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var soundOn: Boolean = false

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
            d.setOnCancelListener { onPlayButtonClick(continueButton) }

        }
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        arguments!!.let {
            gridSize = GridSize.valueOf(it.getString(GRID_SIZE, GridSize.NORMAL.name))
            gameSpeed = GameSpeed.valueOf(it.getString(GAME_SPEED, GameSpeed.FAST.name))
            soundOn = it.getBoolean(SOUND_ON)
        }

        homeButton.setOnClickListener { view: View -> onHomeButtonClick(view) }
        settingsButton.setOnClickListener { view: View -> onSettingsButtonClick(view) }
        continueButton.setOnClickListener { view: View -> onPlayButtonClick(view) }
        restartButton.setOnClickListener { view: View -> onRestartButtonClick(view) }

    }

    private fun onHomeButtonClick(view: View) {
        makeButtonSound()
        listener.onPauseDialogListenerReturnHomeClick()
    }

    private fun onSettingsButtonClick(view: View) {
        makeButtonSound()
        val fm = requireFragmentManager()
        SettingsDialog.newInstance(fm, gridSize, gameSpeed, soundOn)
    }

    private fun onPlayButtonClick(view: View) {
        makeButtonSound()
        listener.onPauseDialogListenerResumeButtonClick()
        this.dismiss()
    }

    private fun onRestartButtonClick(view: View) {
        makeButtonSound()
        listener.onPauseDialogListenerRestartButtonClick()
        this.dismiss()
    }

    private fun makeButtonSound() = runBlocking(Dispatchers.Default) {
        GlobalScope.async {
            MusicPlayerService.playButtonSound(requireContext())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PauseDialogListener) {
            listener = context
        } else {
            throw ClassCastException("${context.packageName} must implement SettingsDialogListener's onSaveSettingDialog")
        }
    }

    /**
     * In case the settings value change (Parent activity listener) we update the pause dialog values
     * for the next time we call settigs dialog
     * @param gridSize Is the size of the grid
     * @param gameSpeed Specifies the speed of simon
     * @param soundOn Specifies whether or not the game will play fx sounds
     */
    fun updateValues(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean) {
        this.soundOn = soundOn
        this.gridSize = gridSize
        this.gameSpeed = gameSpeed
    }

    interface PauseDialogListener : SettingsDialog.SettingsDialogListener {
        fun onPauseDialogListenerResumeButtonClick()
        fun onPauseDialogListenerReturnHomeClick()
        fun onPauseDialogListenerRestartButtonClick()
    }

    companion object {
        @JvmStatic
        fun newInstance(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean): PauseDialog {
            val pauseDialog = PauseDialog().apply {
                arguments = Bundle().apply {
                    putString(GRID_SIZE, gridSize.name)
                    putString(GAME_SPEED, gameSpeed.name)
                    putBoolean(SOUND_ON, soundOn)
                }
            }
            return pauseDialog
        }
    }
}