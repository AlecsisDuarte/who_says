package sh.now.alecsisduart.who_says

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.big_board.*
import kotlinx.android.synthetic.main.game_board_activity.*
import kotlinx.android.synthetic.main.normal_board.*
import kotlinx.coroutines.*
import sh.now.alecsisduart.who_says.bots.SimonBot
import sh.now.alecsisduart.who_says.dialogs.ConfirmDialog
import sh.now.alecsisduart.who_says.dialogs.PauseDialog

import sh.now.alecsisduart.who_says.enums.GridSize
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.GooglePlayServicesHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import sh.now.alecsisduart.who_says.models.AccomplishmentsModel
import sh.now.alecsisduart.who_says.services.MusicPlayerService

private const val MAX_SPEED = 10f

private const val TAG = "GameBoardActivity"

class GameBoardActivity : AppCompatActivity(), PauseDialog.PauseDialogListener, SimonBot.SimonBotListener {

    private lateinit var mMusicPlayerHelper: MusicPlayerHelper
    private lateinit var mConfigurationHelper: ConfigurationHelper

    //Flag to know if we need to restart the game
    private var gameSettingsChanged = false

    //Game values
    private var isSimonTurn: Boolean = true
    private var steps: Int = 1
    private var speed: Float = 1f

    //Animations Durations
    private var shortAnimationDuration: Int = 0
    private var longAnimationDuration: Int = 0
    private var mediumAnimationDuration: Int = 0

    //Job that handles random button pressing (hovering)
    private var simonBot: SimonBot? = null

    //Buttons metadata
    private lateinit var buttonsIds: Map<ImageButton, Int>
    private lateinit var normalGridButtons: List<ImageButton>
    private lateinit var bigGridButtons: List<ImageButton>

    private lateinit var pauseDialog: PauseDialog
    private var confirmDialog: ConfirmDialog? = null

    //Player data
    private lateinit var buttonsToPress: Array<Int>
    private var curButtonToPress: Int = 0
    private var curScore = 0
    private var accomplishments = AccomplishmentsModel()

    //Google Play Services
    private lateinit var mGooglePlayerHelper: GooglePlayServicesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board_activity)

        mConfigurationHelper = ConfigurationHelper.getInstance(this)
        mMusicPlayerHelper = MusicPlayerHelper.getInstance(this)
        mGooglePlayerHelper = GooglePlayServicesHelper.getInstance(this)

        MusicPlayerService.startMusic(this, false)

        if (mConfigurationHelper.gridSize == GridSize.BIG && gameBoardFlipper.displayedChild == 0) {
            gameBoardFlipper.displayedChild = 1
        }

        initializeAnimationsDurations()
        initializeButtonsReferences()
        restartGame()

    }

    private fun initializeButtonsReferences() {
        bigGridButtons = listOf(
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
        normalGridButtons = listOf(
            normalYellowButton,
            normalRedButton,
            normalGreenButton,
            normalBlueButton
        )
        buttonsIds = mapOf(
            normalYellowButton to MusicPlayerHelper.YELLOW_BUTTON_ID,
            bigYellowButton to MusicPlayerHelper.YELLOW_BUTTON_ID,
            normalRedButton to MusicPlayerHelper.RED_BUTTON_ID,
            bigRedButton to MusicPlayerHelper.RED_BUTTON_ID,
            normalGreenButton to MusicPlayerHelper.GRAY_BUTTON_ID,
            bigGreenButton to MusicPlayerHelper.GREEN_BUTTON_ID,
            normalBlueButton to MusicPlayerHelper.BLUE_BUTTON_ID,
            bigBlueButton to MusicPlayerHelper.BLUE_BUTTON_ID,
            bigOrangeButton to MusicPlayerHelper.ORANGE_BUTTON_ID,
            bigLimeButton to MusicPlayerHelper.LIME_BUTTON_ID,
            bigGrayButton to MusicPlayerHelper.GRAY_BUTTON_ID,
            bigPurpleButton to MusicPlayerHelper.PURPLE_BUTTON_ID,
            bigTealButton to MusicPlayerHelper.TEAL_BUTTON_ID
        )
    }


    //Animations
    private fun initializeAnimationsDurations() {
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        mediumAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        longAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
    }

    private fun turnViewFadeOut() {
        turnLabelId.apply {
            alpha = 1f
            visibility = View.VISIBLE

            animate()
                .alpha(0f)
                .duration = shortAnimationDuration.toLong()
        }

    }

    private fun turnViewFadeIn() {
        turnLabelId.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .duration = shortAnimationDuration.toLong()

        }
    }

    //Loaders
    fun changeTextViewColor(textView: TextView, colorId: Int) {
        textView.setTextColor(ContextCompat.getColor(this, colorId))
    }

    fun readySetGoTextAnimation(onFinish: () -> Unit) = runBlocking {
        GlobalScope.launch(Dispatchers.Main) {
            val waitDuration = 600L
            val firstTextView = turnSwitcher.getChildAt(0) as TextView
            val secondTextView = turnSwitcher.getChildAt(1) as TextView
            val originalColor = firstTextView.textColors

            turnViewFadeOut()

            changeTextViewColor(firstTextView, R.color.simon_red_dark)
            turnSwitcher.displayedChild = 0
            turnSwitcher.setCurrentText(getText(R.string.ready))
            delay(waitDuration)

            changeTextViewColor(secondTextView, R.color.simon_yellow_dark)
            turnSwitcher.setText(getText(R.string.set))
            delay(waitDuration)

            changeTextViewColor(firstTextView, R.color.simon_green_dark)
            turnSwitcher.setText(getText(R.string.go))
            delay(waitDuration)

            turnViewFadeIn()
            turnSwitcher.setText(getText(R.string.simon))
            secondTextView.setTextColor(originalColor)
            firstTextView.setTextColor(originalColor)
            delay(mediumAnimationDuration.toLong())

            onFinish()
        }

    }

    fun exitGame() {
        this.finish()
    }

    //Events
    fun onPauseButtonClick(view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        simonBot?.suspend()
        pauseDialog = PauseDialog.newInstance(supportFragmentManager)

    }

    fun onSimonButtonClick(view: View) {
        val imgbttn = view as ImageButton
        simonButtonSound(imgbttn)

        if (!isSimonTurn) {
            val buttons = if (mConfigurationHelper.gridSize == GridSize.NORMAL) normalGridButtons else bigGridButtons
            val index = buttonsToPress[curButtonToPress]
            if (buttons[index] == imgbttn) {
                ++curButtonToPress
                //Already pressed all buttons
                if (curButtonToPress == buttonsToPress.size) {
                    isSimonTurn = true
                    curButtonToPress = 0
                    mConfigurationHelper.let {
                        curScore += (it.gameSpeed.score * it.gridSize.multiplier).toInt()
                    }
                    scoreSwitcher.setText(curScore.toString())
                    turnSwitcher.setText(getText(R.string.simon))
                    initializeSimon(this)
                }
            } else {
                if (confirmDialog == null || !confirmDialog!!.isActive()) {
                    mMusicPlayerHelper.looserSoundAsync()
                    if (mConfigurationHelper.gridSize == GridSize.NORMAL) {
                        accomplishments.normalScore = curScore
                    } else {
                        accomplishments.bigScore = curScore
                    }
                    checkForAccomplishments()
                    confirmDialog = ConfirmDialog.Builder(applicationContext, supportFragmentManager)
                        .setTitle(getText(R.string.you_want_to_replay))
                        .setMessage(getString(R.string.you_want_to_replay_description))
                        .setConfirmButton(R.string.accept, View.OnClickListener { restartGame() })
                        .setCancelButton(R.string.cancel, View.OnClickListener { exitGame() })
                        .show()

                }
            }

        }
    }

    //Actions
    private fun restartGame() {
        Log.d("SCORE", "restarted")
        scoreSwitcher.setCurrentText(getText(R.string.default_score_main_activity))
        curScore = 0

        isSimonTurn = true
        steps = 1
        speed = mConfigurationHelper.gameSpeed.speed
        buttonsToPress = emptyArray()

        //Acomplishments
        ++accomplishments.plays
        checkForAccomplishments()


        readySetGoTextAnimation {
            initializeSimon(this)
        }

    }

    private fun simonButtonSound(imageButton: ImageButton) {
        buttonsIds[imageButton]?.let {
            mMusicPlayerHelper.simonSoundAsync(it)
        }

    }

    private fun initializeSimon(context: Context) {
        simonBot =
            SimonBot(
                if (mConfigurationHelper.gridSize == GridSize.BIG) bigGridButtons else normalGridButtons,
                buttonsIds,
                buttonsToPress,
                speed,
                context,
                mConfigurationHelper.soundOn
            )
        GlobalScope.async(Dispatchers.Default) {
            simonBot?.start()
        }
    }

    private fun flipBoard() {
        when (mConfigurationHelper.gridSize) {
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

    private fun checkForAccomplishments() {
        accomplishments.apply {
            if (bigScore >= 5 || normalScore >= 5 ) {
                learningTheBasicsAchievement = true
            }
            if (bigScore >= 10 || normalScore >= 10) {
                youGotItAchievement = true
            }
            if (bigScore >= 200 || normalScore >= 200) {
                youGotGoodAtItAchievement = true
            }
            if (normalScore >= 5000 || bigScore >= 500) {
                amazingAchievement = true
            }
        }
        mGooglePlayerHelper.pushAccomplishments(accomplishments, gameBoardContainer)
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
        while (simonBot?.state() != SimonBot.JobState.FINISHED);
        isSimonTurn = false
        turnSwitcher.setText(mConfigurationHelper.displayName)
    }

    override fun onSavedSettingsDialog(gridSizeChanged: Boolean, gameSpeedChanged: Boolean, soundOnChanged: Boolean) {
        if (gridSizeChanged) {
            flipBoard()
            gameSettingsChanged = true
        } else if (gameSpeedChanged) {
            gameSettingsChanged = true
        }
    }

    override fun onResume() {
        super.onResume()
        mGooglePlayerHelper.signInSilently(this)
    }

    override fun onPause() {
        super.onPause()
        if (simonBot?.state() == SimonBot.JobState.SUSPEND) {
            simonBot?.resume()
        }
    }


    override fun onPauseDialogListenerReturnHomeClick() {
        if (confirmDialog == null || !confirmDialog!!.isActive()) {
            confirmDialog = ConfirmDialog.Builder(applicationContext, supportFragmentManager)
                .setTitle(getString(R.string.goind_home))
                .setMessage(getString(R.string.going_home_description))
                .setConfirmButton(R.string.accept, View.OnClickListener { exitGame() })
                .setCancelButton(R.string.cancel)
                .show()
        }
    }

    override fun onPauseDialogListenerResumeButtonClick() {
        if (gameSettingsChanged) {
            gameSettingsChanged = false
            if (simonBot?.state() == SimonBot.JobState.SUSPEND) {
                simonBot?.stop()
            }
            restartGame()
        } else {
            if (simonBot?.state() == SimonBot.JobState.SUSPEND) {
                simonBot?.resume()
            }
        }
    }

    override fun onPauseDialogListenerRestartButtonClick() {
        restartGame()
    }
}
