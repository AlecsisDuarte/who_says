package sh.now.alecsisduart.who_says.helpers

import android.content.Context
import android.content.SharedPreferences
import sh.now.alecsisduart.who_says.enums.GameSpeed
import sh.now.alecsisduart.who_says.enums.GridSize
import sh.now.alecsisduart.who_says.models.ConfigurationModel
import sh.now.alecsisduart.who_says.helpers.SharedPreferencesHelper.defaultPreferences
import sh.now.alecsisduart.who_says.helpers.SharedPreferencesHelper.get
import sh.now.alecsisduart.who_says.helpers.SharedPreferencesHelper.set


//SharedPreferences value keys
private const val CONFIG_GRID_SIZE = "GRID_SIZE"
private const val CONFIG_SOUND_ON = "SOUND_ON"
private const val CONFIG_MUSIC_ON = "MUSIC_ON"
private const val CONFIG_GAME_SPEED = "GAME_SPEED"
private const val CONFIG_DISPLAY_NAME = "DISPLAY_NAME"

//SharedPreferences default values
private const val DEFAULT_DISPLAY_NAME = "Player"

class ConfigurationHelper private constructor(context: Context) {

    private var configurationModel: ConfigurationModel
    private val prefs: SharedPreferences = defaultPreferences(context)

    var soundOn
        get() = configurationModel.soundOn
        set(value) {
            configurationModel.soundOn = value
            prefs[CONFIG_SOUND_ON] = value
        }

    var musicOn
        get() = configurationModel.musicOn
        set(value) {
            configurationModel.musicOn = value
            prefs[CONFIG_MUSIC_ON] = value
        }


    var gridSize
        get() = configurationModel.gridSize
        set(value) {
            configurationModel.gridSize = value
            prefs[CONFIG_GRID_SIZE] = value.name
        }

    var gameSpeed
        get() = configurationModel.gameSpeed
        set(value) {
            configurationModel.gameSpeed = value
            prefs[CONFIG_GAME_SPEED] = value.name
        }

    var displayName
        get() = configurationModel.displayName
        set(value) {
            configurationModel.displayName = value
            prefs[CONFIG_DISPLAY_NAME] = value
        }

    init {
        configurationModel = ConfigurationModel(
            soundOn = prefs[CONFIG_SOUND_ON, true]!!,
            musicOn = prefs[CONFIG_MUSIC_ON, true]!!,
            gridSize = GridSize.valueOf(prefs[CONFIG_GRID_SIZE, GridSize.NORMAL.name]!!),
            gameSpeed = GameSpeed.valueOf(prefs[CONFIG_GAME_SPEED, GameSpeed.NORMAL.name]!!),
            displayName = prefs[CONFIG_DISPLAY_NAME, DEFAULT_DISPLAY_NAME]!!
        )

    }

    companion object : SingletonHolder<ConfigurationHelper, Context>(::ConfigurationHelper)
}