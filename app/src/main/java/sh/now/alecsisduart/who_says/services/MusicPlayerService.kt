package sh.now.alecsisduart.who_says.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Binder
import android.os.Build
import android.os.IBinder
import sh.now.alecsisduart.who_says.R



//Action Filters
private const val ACTION_START: String = "sh.now.alecsisduart.who_says.services.musicplayerservice.START"
private const val ACTION_PLAY: String = "sh.now.alecsisduart.who_says.services.musicplayerservice.PLAY"
private const val ACTION_PLAY_SOUND: String = "sh.now.alecsisduart.who_says.services.musicplayerservice.PLAY_SOUND"
private const val ACTION_STOP: String = "sh.now.alecsisduart.who_says.services.musicplayerservice.STOP"
private const val ACTION_PAUSE: String = "sh.now.alecsisduart.who_says.services.musicplayerservice.PAUSE"

//Parameters Constants String
private const val START_MUSIC: String = "sh.now.alecsisduart.who_says.param.START_MUSIC"
private const val SOUND_NAME: String = "sh.now.alecsisduart.who_says.param.SOUND_NAME"

//Player music default values
private const val LEFT_VOLUME: Float = 1f //All Volume
private const val RIGHT_VOLUME: Float = 1f //AllVolume
private const val PRIORITY: Int = 1 //High Priority
private const val NO_LOOP: Int = 0 //Finite
private const val INFINITE_LOOP: Int = -1 //Infinite
private const val RATE: Float = 1f //Normal speed

private const val BACKGROUND_MUSIC = "BACKGROUND_MUSIC"
private const val BUTTON_SOUND = "BUTTON_SOUND"
private const val YELLOW_SOUND = "YELLOW_SOUND"
private const val BLUE_SOUND = "BLUE_SOUND"
private const val RED_SOUND = "RED_SOUND"
private const val GREEN_SOUND = "GREEN_SOUND"
private const val PURPLE_SOUND = "PURPLE_SOUND"
private const val ORANGE_SOUND = "ORANGE_SOUND"
private const val TEAL_SOUND = "TEAL_SOUND"
private const val LIME_SOUND = "LIME_SOUND"
private const val GRAY_SOUND = "GRAY_SOUND"
private const val HIGH_SOUND = "HIGH_SOUND"
private const val MIDDLE_SOUND = "MIDDLE_SOUND"
private const val LOW_SOUND = "LOW_SOUND"
private const val LOOSER_SOUND = "LOOSER_SOUND"

private const val SOUND_ID = "SOUND_ID"
private const val RESOURCE = "RESOURCE"


class MusicPlayerService : Service() {

    //Sounds Index
    private var musicMetadata = mutableMapOf(
        BACKGROUND_MUSIC to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.background_music),
        BUTTON_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.button_sound),
        YELLOW_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.yellow_note),
        BLUE_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.blue_note),
        RED_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.red_note),
        GREEN_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.green_note),
        PURPLE_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.purple_note),
        ORANGE_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.orange_note),
        TEAL_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.teal_note),
        LIME_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.lime_note),
        GRAY_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.gray_note),
        HIGH_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.high_note),
        MIDDLE_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.middle_note),
        LOW_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.low_note),
        LOOSER_SOUND to mutableMapOf(SOUND_ID to 0, RESOURCE to R.raw.looser_note)
    )

    private var mSoundPool: SoundPool? = null

    private val binder = MusicPlayerBinder()

    private var musicStreamId: Int? = null

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> {
                val startMusic: Boolean = intent.extras!!.getBoolean(START_MUSIC)
                onActionStart(startMusic)
            }
            ACTION_PAUSE -> onActionPauseMusic()
            ACTION_PLAY -> onActionPlayMusic()
            ACTION_STOP -> onActionStop()
            ACTION_PLAY_SOUND -> {
                intent.extras!!.getString(SOUND_NAME)?.let {
                    onActionPlaySound(it)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initializeSoundPool() {
        mSoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder().setMaxStreams(20)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .build()
        } else {
            //Used deprecated constructor for older android versions
            @Suppress("DEPRECATION")
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
        musicMetadata.map {
            it.value[SOUND_ID] = mSoundPool!!.load(applicationContext, it.value[RESOURCE]!!, PRIORITY)
        }
    }

    fun onActionStart(startMusic: Boolean = false) {
        if (mSoundPool == null) {
            initializeSoundPool()

            if (startMusic) {
                mSoundPool?.setOnLoadCompleteListener { _: SoundPool, sampleId: Int, status: Int ->
                    musicMetadata[BACKGROUND_MUSIC]?.let {
                        if (it[SOUND_ID]!! == sampleId && status == 0) {
                            onActionPlayMusic()
                        }
                    }
                }
            }
        } else if (startMusic) {
            onActionPlayMusic()
        }
    }

    //Music Actions
    fun onActionPauseMusic() {
        onActionPauseSound()
    }

    fun onActionPlayMusic() {
        onActionPlaySound(BACKGROUND_MUSIC, INFINITE_LOOP)
    }

    //Shared actions
    fun onActionStop() {
        this.onDestroy()
    }

    fun onActionPlaySound(soundName: String, loop: Int = NO_LOOP) {
        musicMetadata[soundName]?.let {
            val soundId: Int = it[SOUND_ID]!!
            if (mSoundPool == null) {
                initializeSoundPool()
            }
            val streamId = mSoundPool?.play(soundId, LEFT_VOLUME, RIGHT_VOLUME, PRIORITY, loop, RATE)
            if (soundName == BACKGROUND_MUSIC) {
                musicStreamId = streamId
            }
        }
    }

    fun onActionPauseSound() {
        musicStreamId?.let {
            mSoundPool?.pause(it)
        }
    }

    //Lifecycle
    override fun onDestroy() {
        super.onDestroy()
        mSoundPool?.release()
        mSoundPool = null
    }

    companion object {
        @JvmStatic
        fun startMusic(context: Context, start: Boolean) {
            val intent = Intent(context, MusicPlayerService::class.java).apply {
                action = ACTION_START
                putExtra(START_MUSIC, start)
            }

            startService(context, intent)
        }

        @JvmStatic
        fun pauseMusic(context: Context) {
            val intent = Intent(context, MusicPlayerService::class.java).apply { action = ACTION_PAUSE }
            startService(context, intent)
        }

        @JvmStatic
        fun playMusic(context: Context) {
            val intent = Intent(context, MusicPlayerService::class.java).apply { action = ACTION_PLAY }
            startService(context, intent)
        }

        @JvmStatic
        fun stopMusic(context: Context) {
            val intent = Intent(context, MusicPlayerService::class.java).apply { action = ACTION_STOP }
            startService(context, intent)
        }

        @JvmStatic
        fun playSound(context: Context, soundName: String) {
            val intent = Intent(context, MusicPlayerService::class.java).apply {
                action = ACTION_PLAY_SOUND
                putExtra(SOUND_NAME, soundName)
            }
            startService(context, intent)
        }

        @JvmStatic
        private fun startService(context: Context, intent: Intent) {

            context.startService(intent)
        }

        @JvmStatic
        fun playButtonSound(context: Context) {
            playSound(context, BUTTON_SOUND)
        }

        @JvmStatic
        fun playYellowButton(context: Context) {
            playSound(context, YELLOW_SOUND)
        }

        @JvmStatic
        fun playRedButton(context: Context) {
            playSound(context, RED_SOUND)
        }

        @JvmStatic
        fun playBlueButton(context: Context) {
            playSound(context, BLUE_SOUND)
        }

        @JvmStatic
        fun playGreenButton(context: Context) {
            playSound(context, GREEN_SOUND)
        }

        @JvmStatic
        fun playOrangeButton(context: Context) {
            playSound(context, ORANGE_SOUND)
        }

        @JvmStatic
        fun playPurpleButton(context: Context) {
            playSound(context, PURPLE_SOUND)
        }

        @JvmStatic
        fun playTealButton(context: Context) {
            playSound(context, TEAL_SOUND)
        }

        @JvmStatic
        fun playGrayButton(context: Context) {
            playSound(context, GRAY_SOUND)
        }

        @JvmStatic
        fun playLimeButton(context: Context) {
            playSound(context, LIME_SOUND)
        }

        @JvmStatic
        fun playHighSound(context: Context) {
            playSound(context, HIGH_SOUND)
        }

        @JvmStatic
        fun playMiddleSound(context: Context) {
            playSound(context, MIDDLE_SOUND)
        }

        @JvmStatic
        fun playLowSound(context: Context) {
            playSound(context, LOW_SOUND)
        }

        @JvmStatic
        fun playLooserSound(context: Context) {
            playSound(context, LOOSER_SOUND)
        }

    }


}
