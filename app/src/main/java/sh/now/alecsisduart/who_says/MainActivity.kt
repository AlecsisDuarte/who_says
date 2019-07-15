package sh.now.alecsisduart.who_says

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.now.alecsisduart.who_says.dialogs.InformationDialog
import sh.now.alecsisduart.who_says.dialogs.SettingsDialog
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.GooglePlayServicesHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import sh.now.alecsisduart.who_says.services.MusicPlayerService

class MainActivity : AppCompatActivity() {

    internal val TAG = "WhoSaysHome"

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

    private fun setBackgroundAnimation() {
        val animBackground = backgroundLayout.background!! as AnimationDrawable
        animBackground.setEnterFadeDuration(10)
        animBackground.setExitFadeDuration(2500)
        animBackground.start()
    }

    private fun changeSoundButtonImage() = runBlocking<Unit> {
        GlobalScope.launch(Dispatchers.Main) {
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


    //Events
    fun onMusicButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        mConfigurationHelper.musicOn = !mConfigurationHelper.musicOn
        changeSoundButtonImage()
        toggleMusic()
    }

    fun onScoreBoardClick(@Suppress("UNUSED_PARAMETER") view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.showScoreBoards()
    }

    fun onConfigurationClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive()) {
            mMusicPlayerHelper.buttonSoundAsync()
            val fm: FragmentManager = supportFragmentManager
            mConfigurationHelper.let {
                settingsDialog = SettingsDialog.newInstance(fm)
            }
        }
    }

    fun onGooglePlayButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.startSignInIntent()
    }

    fun onSimonButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive() && !goingToGame) {
            goingToGame = true
            mMusicPlayerHelper.buttonSoundAsync()
            startActivity(Intent(this, GameBoardActivity::class.java))

        }
    }

    fun onAchievementsClick(@Suppress("UNUSED_PARAMETER") view: View) {
        mMusicPlayerHelper.buttonSoundAsync()
        googlePlayServicesHelper.showAchievements()
    }

    fun onInformationClick(@Suppress("UNUSED_PARAMETER") view: View) {
        mMusicPlayerHelper.highSoundAsync()
        if (informationDialog == null || !informationDialog!!.isActive) {
            informationDialog = InformationDialog.newInstance(supportFragmentManager)
        }

    }

    //Listeners
    private fun onGoogleGamesConnected(@Suppress("UNUSED_PARAMETER") googleSignInAccount: GoogleSignInAccount) {
        hideGooglePlayButton()
    }


    private fun onGoogleGamesDisconnected() {
        hideGooglePlayButton(false)
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
