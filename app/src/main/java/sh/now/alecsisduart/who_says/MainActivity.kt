package sh.now.alecsisduart.who_says

import android.content.*
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.games.*
import com.google.android.gms.games.event.EventBuffer
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.sound_button
import kotlinx.coroutines.*
import sh.now.alecsisduart.who_says.dialogs.InformationDialog
import sh.now.alecsisduart.who_says.dialogs.SettingsDialog
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.GooglePlayServicesHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import sh.now.alecsisduart.who_says.services.MusicPlayerService
import sh.now.alecsisduart.who_says.utils.ExceptionUtils

class MainActivity : AppCompatActivity(), SettingsDialog.SettingsDialogListener {

    internal val TAG = "WhoSaysHome"

    // Client used to sign in with Google APIs
    private var mGoogleSignInClient: GoogleSignInClient? = null

    // Client variables
    private var mEventsClient: EventsClient? = null

    // request codes we use when invoking an external activity
    private val RC_UNUSED = 5001
    private val RC_SIGN_IN = 9001

    //Game Data
    private lateinit var mConfigurationHelper: ConfigurationHelper
    private lateinit var mMusicPlayerHelper: MusicPlayerHelper

    //GooglePlayServices
    private lateinit var googlePlayServicesHelper: GooglePlayServicesHelper

    //Dialogs
    private var settingsDialog: SettingsDialog? = null
    private var informationDialog: InformationDialog? = null
    private var goingToGame: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        googlePlayServicesHelper = GooglePlayServicesHelper.getInstance(this)

        mConfigurationHelper = ConfigurationHelper.getInstance(this)
        mMusicPlayerHelper = MusicPlayerHelper.getInstance(this)

        setBackgroundAnimation()
        changeSoundButtonImage()

    }

    /**
     * We check all the game events
     */
    private fun loadAndPrintEvents() {
        mEventsClient?.load(true)
            ?.addOnSuccessListener { annotateData: AnnotatedData<EventBuffer> ->
                val eventBuffer: EventBuffer? = annotateData.get()

                var count = 0
                eventBuffer?.let { count = eventBuffer.count }

                for (i in count - 1 downTo 0) {
                    eventBuffer?.get(i)!!.let {
                        Log.i(TAG, "event: ${it.name} -> ${it.value}")
                    }
                }
            }
            ?.addOnFailureListener {
                ExceptionUtils.handle(this, it, getString(R.string.events_exception))
            }

    }

    private fun setBackgroundAnimation() {
        val animBackground = backgroundLayout.background!! as AnimationDrawable
        animBackground.setEnterFadeDuration(10)
        animBackground.setExitFadeDuration(2500)
        animBackground.start()
    }

    private fun changeSoundButtonImage() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Main) {
            if (mConfigurationHelper.musicOn) {
                sound_button.setImageResource(R.mipmap.music_on_icon)
            } else {
                sound_button.setImageResource(R.mipmap.music_off_icon)
            }
        }
    }

    private fun toggleMusic() {
        if (mConfigurationHelper.musicOn) {
            MusicPlayerService.playMusic(applicationContext)
        } else {
            MusicPlayerService.pauseMusic(applicationContext)
        }
    }


    //Events
    fun onMusicButtonClick(view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        mConfigurationHelper.musicOn = !mConfigurationHelper.musicOn
        changeSoundButtonImage()
        toggleMusic()
    }

    fun onScoreBoardClick(view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.showScoreBoards()
    }

    fun onConfigurationClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive()) {
            mMusicPlayerHelper.buttonSoundAsync()
            val fm: FragmentManager = supportFragmentManager
            mConfigurationHelper.let {
                settingsDialog = SettingsDialog.newInstance(fm)
            }
        }
    }

    fun onGooglePlayButtonClick(view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.startSignInIntent()
    }

    fun onSimonButtonClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive() && !goingToGame) {
            goingToGame = true
            mMusicPlayerHelper.buttonSoundAsync()
            startActivity(Intent(this, GameBoardActivity::class.java))

        }
    }

    fun onAchievementsClick(view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.showAchievements()
    }

    fun onInformationClick(view: View) {
        mMusicPlayerHelper.highSoundAsync()
        if (informationDialog == null || !informationDialog!!.isActive) {
            informationDialog = InformationDialog.newInstance(supportFragmentManager)
        }

    }


    private fun onGoogleGamesConnected(googleSignInAccount: GoogleSignInAccount) {
        hideGooglePlayButton()
    }


    private fun onGoogleGamesDisconnected() {
        hideGooglePlayButton(false)
    }

    private fun hideGooglePlayButton(hide: Boolean = true) {
        if (hide) {
            googlePlayButton.visibility = View.GONE
            scoreBoardButton.visibility = View.VISIBLE
            achievementsButton.visibility = View.VISIBLE
        } else {
            googlePlayButton.visibility = View.VISIBLE
            scoreBoardButton.visibility = View.GONE
            achievementsButton.visibility = View.GONE
        }
    }

    //Listeners
    override fun onSavedSettingsDialog(gridSizeChanged: Boolean, gameSpeedChanged: Boolean, soundOnChanged: Boolean) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GooglePlayServicesHelper.RC_SIGN_IN -> {
                googlePlayServicesHelper.signInIntentResult(data, supportFragmentManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        googlePlayServicesHelper
            .attachOnConnectedListener(::onGoogleGamesConnected)
            .attachOnDisconnectedListener(::onGoogleGamesDisconnected)
            .signInSilently(this)

        goingToGame = false
        MusicPlayerService.startMusic(this, mConfigurationHelper.musicOn)
    }

    override fun onPause() {
        super.onPause()
        MusicPlayerService.pauseMusic(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerService.stopMusic(this)
    }
}
