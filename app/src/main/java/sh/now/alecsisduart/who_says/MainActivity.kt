package sh.now.alecsisduart.who_says

import android.content.*
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.*
import com.google.android.gms.games.event.EventBuffer
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.sound_button
import kotlinx.coroutines.*
import sh.now.alecsisduart.who_says.dialogs.SettingsDialog
import sh.now.alecsisduart.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import sh.now.alecsisduart.who_says.models.AccomplishmentsModel
import sh.now.alecsisduart.who_says.services.MusicPlayerService
import sh.now.alecsisduart.who_says.utils.ExceptionUtils

class MainActivity : AppCompatActivity(), SettingsDialog.SettingsDialogListener {

    internal val TAG = "WhoSaysHome"

    // Client used to sign in with Google APIs
    private var mGoogleSignInClient: GoogleSignInClient? = null

    // Client variables
    private var mAchievementsClient: AchievementsClient? = null
    private var mLeaderboardsClient: LeaderboardsClient? = null
    private var mEventsClient: EventsClient? = null
    private var mPlayersClient: PlayersClient? = null

    // request codes we use when invoking an external activity
    private val RC_UNUSED = 5001
    private val RC_SIGN_IN = 9001

    // achievements and scores we're pending to push to the cloud
    private val mAccomplishments = AccomplishmentsModel()

    //Game Data
    private lateinit var mConfigurationHelper: ConfigurationHelper
    private lateinit var mMusicPlayerHelper: MusicPlayerHelper


    //Dialogs
    private var settingsDialog: SettingsDialog? = null
    private var goingToGame: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mGoogleSignInClient =
            GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                    .requestEmail()
                    .build()
            )

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

    private fun isSignedIn() : Boolean = GoogleSignIn.getLastSignedInAccount(this) != null

    private fun signInSilently() {
        Log.d(TAG, "signInSilently")

        mGoogleSignInClient?.silentSignIn()?.addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Log.d(TAG, "signInSilently: success")
                onConnected(it.result!!)
            } else {
                onDisconnected()
            }
        }
    }

    private fun startSignInIntent() = mGoogleSignInClient?.let {
        startActivityForResult(it.signInIntent, RC_SIGN_IN)
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

    private fun signOut() {
        Log.d(TAG, "signOut()")
        if (!isSignedIn()) {
            Log.w(TAG, "signOut() callet, but was not signed in!")
            return
        }
        mGoogleSignInClient?.signOut()?.addOnCompleteListener {
            val successful = it.isSuccessful
            Log.d(TAG, "signOut(): ${if (successful) "success" else "failed"}")
            onDisconnected()
        }
    }

    private fun showScoreBoards() {
        mLeaderboardsClient?.allLeaderboardsIntent
            ?.addOnSuccessListener {
                startActivityForResult(intent, RC_UNUSED)
            }
            ?.addOnFailureListener {
                ExceptionUtils.handle(this, it, getString(R.string.leaderboards_exception))
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
        showScoreBoards()
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
        startSignInIntent()
    }

    fun onSimonButtonClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive() && !goingToGame) {
            goingToGame = true
            mMusicPlayerHelper.buttonSoundAsync()
            startActivity(Intent(this, GameBoardActivity::class.java))

        }
    }

    private fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google API's")

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount)
        mEventsClient = Games.getEventsClient(this, googleSignInAccount)
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount)

        hideGooglePlayButton()

        mPlayersClient?.currentPlayer?.addOnCompleteListener {
            if (it.isSuccessful) {
                mConfigurationHelper.displayName = it.result!!.displayName
            } else {
                val exception = it.exception!!
                ExceptionUtils.handle(this, exception, getString(R.string.players_exception))
            }
            //TODO: REMOVE THIS
            Toast.makeText(this, mConfigurationHelper.displayName, Toast.LENGTH_SHORT).show()
        }

        loadAndPrintEvents()

    }


    private fun onDisconnected() {
        Log.d(TAG, "onDisconnected()")

        mLeaderboardsClient = null
        mAchievementsClient = null
        mPlayersClient = null

        hideGooglePlayButton(false)

    }

    private fun hideGooglePlayButton(hide: Boolean = true) {
        if (hide) {
            googlePlayButton.visibility = View.GONE
            scoreBoardButton.visibility = View.VISIBLE
        } else {
            googlePlayButton.visibility = View.VISIBLE
            scoreBoardButton.visibility = View.GONE
        }
    }

    //Listeners
    override fun onSavedSettingsDialog(gridSizeChanged: Boolean, gameSpeedChanged: Boolean, soundOnChanged: Boolean) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)
                    onConnected(account!!)
                } catch (apiException: ApiException) {
                    var message = apiException.message
                    if (message.isNullOrEmpty()) {
                        message = getString(R.string.signin_other_error)
                    }
                    Log.e(TAG, message, apiException)
                    onDisconnected()
                    AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(R.string.ok, null)
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        signInSilently()
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
