package sh.now.alecsisduarte.who_says.models

import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize

data class ConfigurationModel(
    var soundOn: Boolean = false,
    var musicOn: Boolean = false,
    var gridSize: GridSize = GridSize.NORMAL,
    var gameSpeed: GameSpeed = GameSpeed.NORMAL,
    var displayName: String = "Player"
)