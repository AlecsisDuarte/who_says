package sh.now.alecsisduarte.who_says

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kotlinx.android.synthetic.main.big_board.*
import kotlinx.android.synthetic.main.game_board_activity.*
import kotlinx.android.synthetic.main.normal_board.*
import kotlinx.coroutines.*
import sh.now.alecsisduarte.who_says.bots.SimonBot
import sh.now.alecsisduarte.who_says.dialogs.ConfirmDialog
import sh.now.alecsisduarte.who_says.dialogs.PauseDialog

import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.services.MusicPlayerService

const val RETURN_HOME_DIALOG_ID = 1
private const val RETRY_GAME_DIALOG_ID = 2
private const val MAX_SPEED = 10f

class GameBoardActivity : AppCompatActivity(), PauseDialog.PauseDialogListener, SimonBot.SimonBotListener,
    ConfirmDialog.ConfirmDialogListener {

    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var soundOn: Boolean = true
    private var gameSettingsChanged = false

    //Game values
    private var isSimonTurn: Boolean = true
    private var steps: Int = 1
    private var speed: Float = 1f

    private lateinit var simonBot: SimonBot

    //Buttons metadata
    private lateinit var gameButtons: List<ImageButton>

    private val buttonsSounds: List<(Context) -> Unit> = listOf(
        { context: Context -> MusicPlayerService.playYellowButton(context) },
        { context: Context -> MusicPlayerService.playRedButton(context) },
        { context: Context -> MusicPlayerService.playGreenButton(context) },
        { context: Context -> MusicPlayerService.playBlueButton(context) },
        { context: Context -> MusicPlayerService.playOrangeButton(context) },
        { context: Context -> MusicPlayerService.playLimeButton(context) },
        { context: Context -> MusicPlayerService.playGrayButton(context) },
        { context: Context -> MusicPlayerService.playPurpleButton(context) },
        { context: Context -> MusicPlayerService.playTealButton(context) }
    )

    private lateinit var pauseDialog: PauseDialog
    private var confirmDialog: ConfirmDialog? = null

    //Player data
    private lateinit var buttonsToPress: Array<Int>
    private var curButtonToPress: Int = 0
    private var curScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board_activity)

        getConfiguration()

        MusicPlayerService.startMusic(this, false)

        if (gridSize == GridSize.BIG && gameBoardFlipper.displayedChild == 0) {
            gameBoardFlipper.showNext()
        }
        createButtonsArray()

        restartGame()
    }

    //Loaders
    private fun createButtonsArray() {

        gameButtons = if (gridSize == GridSize.NORMAL) {
            listOf(
                normalYellowButton,
                normalRedButton,
                normalGreenButton,
                normalBlueButton
            )
        } else {
            listOf(
                bigYellowButton,
                bigRedButton,
                bigGreenButton,
                bigBlueButton,
                bigOrangeButton,
                bigLimeButton,
                bigGrayButton,
                bigPurpleButton,
                bigTealButton
            )
        }
    }

    fun getConfiguration() {
        baseContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).let {
            gridSize = GridSize.valueOf(it.getString(CONFIG_GRID_SIZE, GridSize.NORMAL.name)!!)
            gameSpeed = GameSpeed.valueOf(it.getString(CONFIG_GAME_SPEED, GameSpeed.NORMAL.name)!!)
            soundOn = it.getBoolean(CONFIG_SOUND_ON, true)
        }

    }

    fun saveConfiguration() {
        applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).let { sp: SharedPreferences ->
            sp.edit {
                this.putString(CONFIG_GRID_SIZE, gridSize.name)
                this.putString(CONFIG_GAME_SPEED, gameSpeed.name)
                this.putBoolean(CONFIG_SOUND_ON, soundOn)
                this.commit()
            }
        }
    }

    //Events
    fun onPauseButtonClick(view: View) {
        makeButtonSound(this)
        simonBot.suspend()

        pauseDialog = PauseDialog.newInstance(gridSize, gameSpeed, soundOn)
        pauseDialog.show(supportFragmentManager, PauseDialog::class.java.simpleName)

    }

    fun onSimonButtonClick(view: View) {
        val imgbttn = view as ImageButton
        simonButtonSound(imgbttn, this)

        if (!isSimonTurn) {

            val index = buttonsToPress[curButtonToPress]
            if (gameButtons[index] == imgbttn) {
                ++curButtonToPress
                //Already pressed all buttons
                if (curButtonToPress == buttonsToPress.size) {
                    curButtonToPress = 0
                    curScore += (gameSpeed.score * gridSize.multiplier).toInt()
                    val score = curScore.toString()
                    scoreView.text = score
                    Log.d("SCORE", score)
                    playerView.text = getText(R.string.simon)
                    initializeSimon(this)
                }
            } else {
                if (confirmDialog == null || !confirmDialog!!.isActive()) {
                    makeLooserSound(this)
                    confirmDialog = ConfirmDialog.newInstance(
                        supportFragmentManager,
                        getString(R.string.you_want_to_replay),
                        getString(R.string.you_want_to_replay_description),
                        RETRY_GAME_DIALOG_ID
                    )
                }
            }

        }
    }

    //Actions
    private fun restartGame() {
        Log.d("SCORE", "restarted")
        scoreView.text = getText(R.string.default_score_main_activity)
        curScore = 0

        isSimonTurn = true
        steps = 1
        speed = gameSpeed.speed
        buttonsToPress = emptyArray()

        playerView.text = getText(R.string.simon)
        initializeSimon(this)

    }

    private fun initializeSimon(context: Context) {
        simonBot = SimonBot(gameButtons, buttonsSounds, buttonsToPress, steps, speed, context, soundOn)
        GlobalScope.async(Dispatchers.Default) {
            simonBot.start()
        }
    }

    private fun flipBoard() {
        when (gridSize) {
            GridSize.NORMAL -> {
                if (gameBoardFlipper.displayedChild != 0) {
                    gameBoardFlipper.let {
                        it.inAnimation = AnimationUtils.loadAnimation(this, R.anim.in_from_left)
                        it.outAnimation = AnimationUtils.loadAnimation(this, R.anim.out_to_right)
                        it.showNext()
                    }
                }
            }
            GridSize.BIG -> {
                if (gameBoardFlipper.displayedChild != 1) {
                    gameBoardFlipper.let {
                        it.inAnimation = AnimationUtils.loadAnimation(this, R.anim.in_from_right)
                        it.outAnimation = AnimationUtils.loadAnimation(this, R.anim.out_to_left)
                        it.showPrevious()
                    }
                }
            }
        }
    }

    //Listeners
    override fun onSimonBotFinished(buttonsPressed: Array<Int>) {
        buttonsToPress = buttonsPressed
        curButtonToPress = 0

        //We speed up and increase the amount of buttons to press
        ++steps
        if (speed < MAX_SPEED) {
            speed += 0.1f
        }
        while (simonBot.state() != SimonBot.JobState.FINISHED);
        isSimonTurn = false
        playerView.text = getText(R.string.your_turn)
    }

    override fun onSavedSettingsDialog(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean) {
        this.soundOn = soundOn

        pauseDialog.updateValues(gridSize, gameSpeed, soundOn)

        if (this.gridSize != gridSize) {
            this.gridSize = gridSize
            createButtonsArray()
            flipBoard()
            gameSettingsChanged = true
        } else if (this.gameSpeed != gameSpeed) {
            gameSettingsChanged = true
        }

        this.gameSpeed = gameSpeed

        saveConfiguration()

    }

    override fun onConfirmDialogCancelClick(id: Int) {
        when (id) {
            RETRY_GAME_DIALOG_ID -> {
                this.finish()

            }
            RETURN_HOME_DIALOG_ID -> {
            }
        }
    }

    override fun onConfirmDialogConfirmClick(id: Int) {
        when (id) {
            RETRY_GAME_DIALOG_ID -> {
                restartGame()
            }
            RETURN_HOME_DIALOG_ID -> {
                pauseDialog.dismiss()
                this.finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (simonBot.state() == SimonBot.JobState.SUSPEND) {
            simonBot.resume()
        }
    }


    override fun onPauseDialogListenerReturnHomeClick() {
        if (confirmDialog == null || !confirmDialog!!.isActive()) {
            confirmDialog = ConfirmDialog.newInstance(
                supportFragmentManager,
                getString(R.string.goind_home),
                getString(R.string.going_home_description),
                RETURN_HOME_DIALOG_ID
            )
        }
    }

    override fun onPauseDialogListenerResumeButtonClick() {
        if (gameSettingsChanged) {
            gameSettingsChanged = false
            if (simonBot.state() == SimonBot.JobState.SUSPEND) {
                simonBot.stop()
            }
            restartGame()
        } else {
            if (simonBot.state() == SimonBot.JobState.SUSPEND) {
                simonBot.resume()
            }
        }
    }

    override fun onPauseDialogListenerRestartButtonClick() {
        restartGame()
    }

    //Sounds
    fun makeButtonSound(context: Context) = runBlocking {
        if (soundOn) {
            GlobalScope.async(Dispatchers.Default) {
                MusicPlayerService.playButtonSound(context)
            }
        }
    }

    fun simonButtonSound(simonButton: ImageButton, context: Context) = runBlocking {
        if (soundOn) {
            GlobalScope.async(Dispatchers.Default) {
                val index = gameButtons.indexOf(simonButton)
                buttonsSounds[index](context)
            }
        }
    }

    fun makeLooserSound(context: Context) = runBlocking {
        if (soundOn) {
            GlobalScope.async(Dispatchers.Default) {
                MusicPlayerService.playLooserSound(context)
            }
        }
    }
}
