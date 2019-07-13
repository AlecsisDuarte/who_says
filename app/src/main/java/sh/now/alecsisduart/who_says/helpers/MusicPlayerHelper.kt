package sh.now.alecsisduart.who_says.helpers

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import sh.now.alecsisduart.who_says.services.MusicPlayerService

class MusicPlayerHelper private constructor(private val context: Context) {
    private val configurationHelper: ConfigurationHelper = ConfigurationHelper.getInstance(context)

    private val soundOn get() = configurationHelper.soundOn

    fun buttonSoundAsync() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) {
            if (soundOn) {
                MusicPlayerService.playButtonSound(context)
            }
        }
    }

    fun lowSoundAsync() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) {
            if (soundOn) {
                MusicPlayerService.playLowSound(context)
            }
        }
    }

    fun highSoundAsync() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) {
            if (soundOn) {
                MusicPlayerService.playHighSound(context)
            }
        }
    }

    fun middleSoundAsync() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) {
            if (soundOn) {
                MusicPlayerService.playMiddleSound(context)
            }
        }
    }


    fun looserSoundAsync() = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) {
            if (soundOn) {
                MusicPlayerService.playLooserSound(context)
            }
        }
    }


    fun simonSoundAsync(buttonId: Int) = runBlocking<Unit> {
        GlobalScope.async(Dispatchers.Default) { }
        when (buttonId) {
            YELLOW_BUTTON_ID -> MusicPlayerService.playYellowButton(context)
            RED_BUTTON_ID -> MusicPlayerService.playRedButton(context)
            GREEN_BUTTON_ID -> MusicPlayerService.playGreenButton(context)
            BLUE_BUTTON_ID -> MusicPlayerService.playBlueButton(context)
            ORANGE_BUTTON_ID -> MusicPlayerService.playOrangeButton(context)
            LIME_BUTTON_ID -> MusicPlayerService.playLimeButton(context)
            GRAY_BUTTON_ID -> MusicPlayerService.playGrayButton(context)
            PURPLE_BUTTON_ID -> MusicPlayerService.playPurpleButton(context)
            TEAL_BUTTON_ID -> MusicPlayerService.playTealButton(context)
        }
    }

    companion object : SingletonHolder<MusicPlayerHelper, Context>(::MusicPlayerHelper) {
        const val YELLOW_BUTTON_ID = 0
        const val RED_BUTTON_ID = 1
        const val GREEN_BUTTON_ID = 2
        const val BLUE_BUTTON_ID = 3
        const val ORANGE_BUTTON_ID = 4
        const val LIME_BUTTON_ID = 5
        const val GRAY_BUTTON_ID = 6
        const val PURPLE_BUTTON_ID = 7
        const val TEAL_BUTTON_ID = 8
    }
}