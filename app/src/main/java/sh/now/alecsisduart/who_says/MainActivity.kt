package sh.now.alecsisduarte.who_says

import android.content.*
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.*
import com.google.android.gms.games.event.EventBuffer
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.sound_button
import kotlinx.coroutines.*
import sh.now.alecsisduarte.who_says.dialogs.SettingsDialog
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.helpers.ConfigurationHelper
import sh.now.alecsisduarte.who_says.models.AccomplishmentsModel
import sh.now.alecsisduarte.who_says.services.MusicPlayerService
import sh.now.alecsisduarte.who_says.utils.ExceptionUtils

const val SHARED_PREF_NAME = "CONFIGURATION"
const val CONFIG_GRID_SIZE = "GRID_SIZE"
const val CONFIG_SOUND_ON = "SOUND_ON"
const val CONFIG_MUSIC_ON = "MUSIC_ON"
const val CONFIG_GAME_SPEED = "GAME_SPEED"

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
    // (waiting for the user to sign in, for instance)
    private val mAccomplishments = AccomplishmentsModel()

    //Game Data
    private lateinit var mConfigurationHelper: ConfigurationHelper
//    private lateinit var gridSize: GridSize
//    private lateinit var gameSpeed: GameSpeed
//    private var musicOn: Boolean = true
//    private var soundOn: Boolean = true


    //Dialogs
    private var settingsDialog: SettingsDialog? = null
    private var goingToGame: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mConfigurationHelper = ConfigurationHelper.getInstance(this)

        mGoogleSignInClient =
            GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build())

        setBackgroundAnimation()
        changeSoundButtonImage()
    }

    /**
     * We check all the game events
     */
    private fun loadAndPrintEvents() {
        val mainActivity: MainActivity = this

        mEventsClient?.load(true)
            ?.addOnSuccessListener { eventBuffer: AnnotatedData<EventBuffer> ->
                val eventBuffer: EventBuffer? = eventBuffer.get()

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

    private fun isSignedIn() = GoogleSignIn.getLastSignedInAccount(this) != null

    private fun signInSilently() {
        Log.d(TAG, "signInSilently")

        mGoogleSignInClient?.silentSignIn()?.addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Log.d(TAG, "signInSilently: success")
                onConnected(it.result!!)
            } else {
                Log.d(TAG, "signInSilently: failure", it.exception)
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

    private fun buttonSoundEffect() = runBlocking {
        if (mConfigurationHelper.soundOn) {
            GlobalScope.launch {
                MusicPlayerService.playButtonSound(applicationContext)
            }
        }
    }

    private fun changeSoundButtonImage() = runBlocking {
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
        buttonSoundEffect()
        mConfigurationHelper.musicOn = !mConfigurationHelper.musicOn
        changeSoundButtonImage()
        toggleMusic()
    }

    fun onScoreBoardClick(view: View) {
        buttonSoundEffect()
        Toast.makeText(this, R.string.no_scoreboard, Toast.LENGTH_SHORT).show()
    }

    fun onConfigurationClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive()) {
            buttonSoundEffect()
            val fm: FragmentManager = supportFragmentManager
            mConfigurationHelper.let {
                settingsDialog = SettingsDialog.newInstance(fm, it.gridSize, it.gameSpeed, it.soundOn)
            }
        }
    }

    fun onGooglePlayButtonClick(view: View) = startSignInIntent()

    fun onSimonButtonClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive() && !goingToGame) {
            goingToGame = true
            buttonSoundEffect()
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
        googlePlayButton.visibility = if (hide) View.GONE else View.VISIBLE
        scoreBoardButton.visibility = if (hide) View.VISIBLE else View.GONE
    }

    //Listeners
    override fun onSavedSettingsDialog(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean) {
        mConfigurationHelper.let {
            it.gridSize = gridSize
            it.gameSpeed = gameSpeed
            it.soundOn = soundOn
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
