package sh.now.alecsisduarte.who_says

import android.content.*
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.sound_button
import sh.now.alecsisduarte.who_says.dialogs.SettingsDialog
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.services.MusicPlayerService

val SHARED_PREF_NAME = "CONFIGURATION"
val CONFIG_GRID_SIZE = "GRID_SIZE"
val CONFIG_SOUND_ON = "SOUND_ON"
val CONFIG_MUSIC_ON = "MUSIC_ON"
val CONFIG_GAME_SPEED = "GAME_SPEED"

class MainActivity : AppCompatActivity(), SettingsDialog.SettingsDialogListener {

    private lateinit var mMusicPlayerService: MusicPlayerService
    private var mMusicPlayerBound = false

    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var musicOn: Boolean = true
    private var soundOn: Boolean = true

    private val musicPlayerConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            mMusicPlayerService.onActionStop()
            mMusicPlayerBound = false
        }

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            mMusicPlayerService = binder.getService()
            mMusicPlayerBound = true
            mMusicPlayerService.onActionStart(musicOn)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        getConfiguration()
        setBackgroundAnimation()
        changeSoundButtonImage()

        MusicPlayerService.startMusic(this, musicOn)
    }

    fun getConfiguration() {
        val sp: SharedPreferences = baseContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        gridSize = GridSize.valueOf(sp.getString(CONFIG_GRID_SIZE, GridSize.NORMAL.name)!!)
        gameSpeed = GameSpeed.valueOf(sp.getString(CONFIG_GAME_SPEED, GameSpeed.NORMAL.name)!!)
        musicOn = sp.getBoolean(CONFIG_MUSIC_ON, true)
        soundOn = sp.getBoolean(CONFIG_SOUND_ON, true)

    }

    fun setBackgroundAnimation() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val animBackground = backgroundLayout.background!! as AnimationDrawable
        animBackground.setEnterFadeDuration(10)
        animBackground.setExitFadeDuration(5000)
        animBackground.start()
    }

    fun buttonSoundEffect() {
        if (soundOn) {
            MusicPlayerService.playButtonSound(this)
        }
    }

    fun changeSoundButtonImage() {
        if (musicOn) {
            sound_button.setImageResource(R.mipmap.sound_on_icon)
        } else {
            sound_button.setImageResource(R.mipmap.volume_off_icon)
        }
    }

    fun toggleMusic() {
        if (musicOn) {
            MusicPlayerService.playMusic(this)
        } else {
            MusicPlayerService.pauseMusic(this)
        }
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
    }

    fun onConfigurationClick(view: View) {
        buttonSoundEffect()
        val fm: FragmentManager = supportFragmentManager
        SettingsDialog.newInstance(fm, gridSize, gameSpeed, soundOn)
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
        if (musicOn) {
            MusicPlayerService.playMusic(this)
        }
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
