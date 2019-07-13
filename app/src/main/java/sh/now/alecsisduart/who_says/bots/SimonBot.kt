package sh.now.alecsisduarte.who_says.bots

import android.content.Context
import android.util.Log
import android.widget.ImageButton
import kotlinx.coroutines.*
import sh.now.alecsisduarte.who_says.bots.SimonBot.JobState.*

class SimonBot(
    private val buttons: List<ImageButton>,
    private val sounds: List<(context: Context) -> Unit>,
    private val previousPressedButtons: Array<Int>,
    private val steps: Int,
    private val speed: Float,
    private val context: Context,
    private val soundOn: Boolean
) {

    enum class JobState {
        RUNNING, SUSPEND, FINISHED
    }

    private var job: Job? = null
    private var jobState: JobState = FINISHED
    private var listener: SimonBotListener

    init {
        if (context is SimonBotListener) {
            listener = this.context
        } else {
            throw Error("${context::class.java.simpleName} must implement SimonBotListener")
        }
    }

    fun start() = runBlocking {
        job = launch(Dispatchers.Main) {
            jobState = RUNNING
            startSimonBot()
        }

    }

    fun state(): JobState {
        return jobState
    }

    fun suspend() {
        jobState = SUSPEND
    }

    fun resume() {
        if (jobState == SUSPEND) {
            jobState = RUNNING
        }
    }

    fun stop() {
        if (jobState == SUSPEND) {
            jobState = FINISHED
            try {
                job?.cancel()
            } catch (ce: CancellationException) {
                Log.d("SIMON_BOT", "simon bot stopped", ce)
            }
        }
    }

    private suspend fun startSimonBot() = withContext(Dispatchers.Default) {
        var step = 0
        val buttonsPressed = Array(steps) { 0 }
        val maxButtonIndex = buttons.size - 1

        delay(800)

        while (step < previousPressedButtons.size) {
            while (jobState == SUSPEND);
            val index = previousPressedButtons[step]
            simonButtonHandling(index)
            buttonsPressed[step++] = index
        }

        while (step < steps) {
            while (jobState == SUSPEND);
            val index = (Math.random() * maxButtonIndex).toInt()
            simonButtonHandling(index)
            buttonsPressed[step++] = index

        }

        jobState = FINISHED
        launch(Dispatchers.Main) {
            listener.onSimonBotFinished(buttonsPressed)
        }
    }

    private suspend fun simonButtonHandling(index: Int) = withContext(Dispatchers.Default) {
        if (soundOn) {
            sounds[index](context)
        }
        GlobalScope.launch(Dispatchers.Main) {
            buttons[index].isHovered = true
            val delay = 250L
            val calcDelay = (1000 / speed).toLong()
            if (delay > calcDelay) {
                calcDelay
            }
            delay(delay)
            buttons[index].isHovered = false
        }
        delay((1000 / speed).toLong())

    }

    interface SimonBotListener {
        fun onSimonBotFinished(buttonsPressed: Array<Int>)
    }
}