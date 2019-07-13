package sh.now.alecsisduart.who_says.bots

import android.content.Context
import android.media.Image
import android.util.Log
import android.widget.ImageButton
import kotlinx.coroutines.*
import sh.now.alecsisduart.who_says.bots.SimonBot.JobState.*
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper
import kotlin.math.max

class SimonBot(
    private val buttons: List<ImageButton>,
    private val buttonsIds: Map<ImageButton, Int>,
    private val previousPressedButtons: Array<Int>,
    private val speed: Float,
    private val context: Context,
    private val soundOn: Boolean
) {

    enum class JobState {
        RUNNING, SUSPEND, FINISHED
    }

    private val mMusicPlayerHelper = MusicPlayerHelper.getInstance(context)

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
        val maxSteps = previousPressedButtons.size + 1
        val buttonsToPress = Array(maxSteps) { 0 }
        val maxButtonIndex = buttons.size - 1


        delay(400)

        while (step < previousPressedButtons.size) {
            while (jobState == SUSPEND);
            val index = previousPressedButtons[step]
            simonButtonHandling(index)
            buttonsToPress[step++] = index
        }


        while (jobState == SUSPEND);

        val index = (Math.random() * maxButtonIndex).toInt()
        simonButtonHandling(index, true)
        buttonsToPress[step] = index

        jobState = FINISHED
        launch(Dispatchers.Main) {
            listener.onSimonBotFinished(buttonsToPress)
        }
    }

    private suspend fun simonButtonHandling(index: Int, isLast: Boolean = false) = withContext(Dispatchers.Default) {
        val button = buttons[index]
        val soundJob = GlobalScope.async {
            if (soundOn) {
                buttonsIds[button]?.let {
                    mMusicPlayerHelper.simonSoundAsync(it)
                }
            }
        }
        val calcDelay = (1000 / speed).toLong()

        val hoverJob = GlobalScope.async(Dispatchers.Main) {
            buttons[index].isHovered = true
            val delay = 100L
            delay(max(delay, calcDelay))
            buttons[index].isHovered = false
        }

        awaitAll(soundJob, hoverJob)
        if (!isLast) {
            delay(calcDelay)
        }

    }

    interface SimonBotListener {
        fun onSimonBotFinished(buttonsPressed: Array<Int>)
    }
}