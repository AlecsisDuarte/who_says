package sh.now.alecsisduart.who_says.helpers

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.*
import sh.now.alecsisduart.who_says.GameBoardActivity
import sh.now.alecsisduart.who_says.R
import sh.now.alecsisduart.who_says.dialogs.ConfirmDialog
import sh.now.alecsisduart.who_says.models.AccomplishmentsModel
import sh.now.alecsisduart.who_says.utils.ExceptionUtils

private const val TAG = "GooglePlayServiceHelper"

class GooglePlayServicesHelper private constructor(private val activity: AppCompatActivity) {

    // Client variables
    private var mAchievementsClient: AchievementsClient? = null
    private var mLeaderboardsClient: LeaderboardsClient? = null
    private var mEventsClient: EventsClient? = null
    private var mPlayersClient: PlayersClient? = null

    private var context: Context = activity.applicationContext

    //Helpers
    private var configurationHelper: ConfigurationHelper

    // Client used to sign in with Google APIs
    private var mGoogleSignInClient: GoogleSignInClient

    //Sign in option
    private val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN

    //Listeners
    private var onDisconnectedListener: (() -> Unit)? = null
    private var onConnectedListener: ((account: GoogleSignInAccount) -> Unit)? = null

    init {
        mGoogleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(signInOptions).build())
        configurationHelper = ConfigurationHelper.getInstance(context)
    }

    /**
     * Returns whether or not the [account] has permissions
     */
    private fun accountHasPermission(account: GoogleSignInAccount?): Boolean {
        if (account != null) {
            return GoogleSignIn.hasPermissions(account, signInOptions.scopes.first())
        }
        return false
    }

    /**
     * Validate if we have the account
     */
    fun isSignedIn(): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

    /**
     * Try to sign in silently
     */
    fun signInSilently(context: Context) {
        this.context = context
        Log.d(TAG, "SignInSilently")
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (accountHasPermission(account)) {
            onConnected(account!!)
        } else {
            mGoogleSignInClient.silentSignIn()?.addOnCompleteListener(activity) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInSilently: Success")
                    onConnected(it.result!!)
                } else {
                    onDisconnected()
                }
            }
        }

    }

    fun startSignInIntent() = mGoogleSignInClient?.let {
        activity.startActivityForResult(it.signInIntent, RC_UNUSED)
    }

    fun showScoreBoards() {
        mLeaderboardsClient?.allLeaderboardsIntent
            ?.addOnSuccessListener {
                activity.startActivityForResult(it, RC_UNUSED)
            }
            ?.addOnFailureListener {
                ExceptionUtils.handle(context, it, context.getString(R.string.signin_other_error))
            }
    }

    fun showAchievements() {
        mAchievementsClient?.achievementsIntent
            ?.addOnSuccessListener {
                activity.startActivityForResult(it, RC_UNUSED)
            }
            ?.addOnFailureListener {
                ExceptionUtils.handle(context, it, context.getString(R.string.achievements_exception))
            }
    }

    fun pushAccomplishments(accomplishmentsModel: AccomplishmentsModel, notificationView: View? = null) {
        var achievementsClient = mAchievementsClient
        var leaderboardsClient = mLeaderboardsClient

        if (!isSignedIn()) {
            return
        } else if (notificationView != null) {
            val gamesClient = Games.getGamesClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            gamesClient.setViewForPopups(notificationView)
        }

        accomplishmentsModel.apply {
            //Achievements
            if (amazingAchievement) {
                achievementsClient.unlockWithResId(R.string.achievement_amazing)
                amazingAchievement = false
            }
            if (learningTheBasicsAchievement) {
                achievementsClient.unlockWithResId(R.string.achievement_learning_the_basics)
                learningTheBasicsAchievement = false
            }
            if (youGotGoodAtItAchievement) {
                achievementsClient.unlockWithResId(R.string.achievement_you_got_good_at_it)
                youGotGoodAtItAchievement = false
            }
            if (youGotItAchievement) {
                achievementsClient.unlockWithResId(R.string.achievement_you_got_it)
                youGotItAchievement = false
            }

            //Increments
            if (plays > 0) {
                achievementsClient.incrementWithResId(R.string.achievement_really_really_bored, plays)
                achievementsClient.incrementWithResId(R.string.achievement_bored, plays)
                plays = 0
            }

            //Scores
            if (normalScore > 0) {
                leaderboardsClient.submitWithResId(R.string.leaderboard_normal_board_high_scores, normalScore)
                normalScore = 0
            }
            if (bigScore > 0) {
                leaderboardsClient.submitWithResId(R.string.leaderboard_big_board_high_scores, bigScore)
                bigScore = 0
            }
        }
    }

    private fun AchievementsClient?.unlockWithResId(resId: Int) {
        this?.unlock(context.getString(resId))
    }

    private fun AchievementsClient?.incrementWithResId(resId: Int, plays: Int) {
        this?.increment(context.getString(resId), plays)
    }

    private fun LeaderboardsClient?.submitWithResId(resId: Int, score: Int) {
        this?.submitScore(context.getString(resId), score.toLong())
    }

    fun attachOnConnectedListener(listener: ((account: GoogleSignInAccount) -> Unit)) = apply {
        onConnectedListener = listener
    }

    fun attachOnDisconnectedListener(listener: (() -> Unit)) = apply {
        onDisconnectedListener = listener
    }

    //Listeners
    private fun onConnected(account: GoogleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google API's")

        mAchievementsClient = Games.getAchievementsClient(context, account)
        mLeaderboardsClient = Games.getLeaderboardsClient(context, account)
        mEventsClient = Games.getEventsClient(context, account)
        mPlayersClient = Games.getPlayersClient(context, account)

        mPlayersClient?.currentPlayer?.addOnCompleteListener {
            if (it.isSuccessful) {
                it.result?.let { configurationHelper.displayName = it.displayName }
            } else {
                ExceptionUtils.handle(context, it.exception!!, context.getString(R.string.players_exception))
            }
        }

        onConnectedListener?.let { it(account) }

    }

    fun signInIntentResult(data: Intent?, fragmentManager: FragmentManager) {
        val res = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (res.isSuccess) {
            res.signInAccount?.let { onConnected(it) }
        } else {
            var message: String? = res.status.statusMessage
            if (message.isNullOrEmpty()) {
                message = context.getString(R.string.signin_other_error)
            }
            onDisconnected()
            ConfirmDialog.Builder(context, fragmentManager)
                .setMessage(message)
                .setConfirmButton(R.string.ok, null)
                .show()
        }
    }

    private fun onDisconnected() {
        Log.d(TAG, "onDisconnected()")
        mLeaderboardsClient = null
        mAchievementsClient = null
        mPlayersClient = null

        onDisconnectedListener?.let { it() }
    }

    companion object : SingletonHolder<GooglePlayServicesHelper, AppCompatActivity>(::GooglePlayServicesHelper) {
        //Request codes
        const val RC_UNUSED = 5001
        const val RC_SIGN_IN = 9001
    }
}