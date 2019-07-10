package sh.now.alecsisduarte.who_says.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import sh.now.alecsisduarte.who_says.R
import sh.now.alecsisduarte.who_says.enums.GameSpeed
import sh.now.alecsisduarte.who_says.enums.GridSize

private const val GRID_SIZE = "sh.now.alecsisduarte.who_says.settings.grid_size"
private const val GAME_SPEED = "sh.now.alecsisduarte.who_says.settings.game_speed"
private const val SOUND_ON = "sh.now.alecsisduarte.who_says.settings.sound_on"

class BoardFragment : Fragment() {
    private lateinit var gridSize: GridSize
    private lateinit var gameSpeed: GameSpeed
    private var soundOn: Boolean = false

    private lateinit var listener: BoardFragmentListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments!!.let {
            gridSize = GridSize.valueOf(it.getString(GRID_SIZE, GridSize.NORMAL.name))
            gameSpeed = GameSpeed.valueOf(it.getString(GAME_SPEED, GameSpeed.FAST.name))
            soundOn = it.getBoolean(SOUND_ON)
        }

        return inflater.inflate(
            when (gridSize) {
                GridSize.BIG -> R.layout.big_board
                GridSize.NORMAL -> R.layout.normal_board
            },
            container
        )
    }


    interface BoardFragmentListener {
        fun simonButtonPressed(index: Int)
    }
}