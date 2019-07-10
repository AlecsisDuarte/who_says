package sh.now.alecsisduarte.who_says

import android.content.*
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.sound_button
import kotlinx.coroutines.*
import sh.now.alecsisduarte.who_says.dialogs.SettingsDialog
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.services.MusicPlayerService

const val SHARED_PREF_NAME = "CONFIGURATION"
const val CONFIG_GRID_SIZE = "GRID_SIZE"
const val CONFIG_SOUND_ON = "SOUND_ON"
const val CONFIG_MUSIC_ON = "MUSIC_ON"
const val CONFIG_GAME_SPEED = "GAME_SPEED"

class MainActivity : AppCompatActivity(), SettingsDialog.SettingsDialogListener {

    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var musicOn: Boolean = true
    private var soundOn: Boolean = true

    private var settingsDialog: SettingsDialog? = null
    private var goingToGame = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        getConfiguration()
        setBackgroundAnimation()
        changeSoundButtonImage()
    }

    private fun getConfiguration() {
        val sp: SharedPreferences = baseContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        gridSize = GridSize.valueOf(sp.getString(CONFIG_GRID_SIZE, GridSize.NORMAL.name)!!)
        gameSpeed = GameSpeed.valueOf(sp.getString(CONFIG_GAME_SPEED, GameSpeed.NORMAL.name)!!)
        musicOn = sp.getBoolean(CONFIG_MUSIC_ON, true)
        soundOn = sp.getBoolean(CONFIG_SOUND_ON, true)

    }

    private fun setBackgroundAnimation() {
        val animBackground = backgroundLayout.background!! as AnimationDrawable
        animBackground.setEnterFadeDuration(10)
        animBackground.setExitFadeDuration(2500)
        animBackground.start()
    }

    private fun buttonSoundEffect() = runBlocking {
        if (soundOn) {
            GlobalScope.launch {
                MusicPlayerService.playButtonSound(applicationContext)
            }
        }
    }

    private fun changeSoundButtonImage() = runBlocking {
        GlobalScope.async(Dispatchers.Main) {
            if (musicOn) {
                sound_button.setImageResource(R.mipmap.music_on_icon)
            } else {
                sound_button.setImageResource(R.mipmap.music_off_icon)
            }
        }
    }

    private fun toggleMusic() {
//        GlobalScope.async(Dispatchers.Default) {
        if (musicOn) {
            MusicPlayerService.playMusic(applicationContext)
        } else {
            MusicPlayerService.pauseMusic(applicationContext)
        }
//        }
    }

    //Events
    fun onMusicButtonClick(view: View) {
        buttonSoundEffect()
        musicOn = !musicOn
        toggleMusic()
        changeSoundButtonImage()
        val sp: SharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sp.edit {
            putBoolean(CONFIG_MUSIC_ON, musicOn)
            commit()
        }
    }

    fun onScoreBoardClick(view: View) {
        buttonSoundEffect()
        Toast.makeText(this, R.string.no_scoreboard, Toast.LENGTH_SHORT).show()
    }

    fun onConfigurationClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive()) {
            buttonSoundEffect()
            val fm: FragmentManager = supportFragmentManager
            settingsDialog = SettingsDialog.newInstance(fm, gridSize, gameSpeed, soundOn)
        }
    }

    fun onSimonButtonClick(view: View) {
        if (settingsDialog == null || !settingsDialog!!.isActive() && !goingToGame) {
            goingToGame = true
            buttonSoundEffect()
            startActivity(Intent(this, GameBoardActivity::class.java))

        }
    }

    override fun onSavedSettingsDialog(gridSize: GridSize, gameSpeed: GameSpeed, soundOn: Boolean) {
        this.gridSize = gridSize
        this.gameSpeed = gameSpeed
        this.soundOn = soundOn

        applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(CONFIG_GRID_SIZE, gridSize.name)
            putString(CONFIG_GAME_SPEED, gameSpeed.name)
            putBoolean(CONFIG_SOUND_ON, soundOn)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
//        getConfiguration()
        goingToGame = false
        MusicPlayerService.startMusic(this, musicOn)
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
