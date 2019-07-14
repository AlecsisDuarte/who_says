package sh.now.alecsisduart.who_says.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.information_dialog.*
import sh.now.alecsisduart.who_says.R
import sh.now.alecsisduart.who_says.helpers.MusicPlayerHelper

private const val TAG = "InformationDialog"

class InformationDialog : DialogFragment() {
    private var active = true
    private lateinit var mMusicPlayerHelper: MusicPlayerHelper

    val isActive get() = active

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.information_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.let { d: Dialog ->
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.window?.let { w: Window ->
                w.setBackgroundDrawableResource(R.color.colorTransparent)
            }

        }

        mMusicPlayerHelper = MusicPlayerHelper.getInstance(requireContext())

        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        privacyPolicyButton.setOnClickListener { openUri(R.string.uri_privacy_policy) }
        aboutMeButton.setOnClickListener { openUri(R.string.uri_about_me) }
        musicCreationButton.setOnClickListener { openUri(R.string.uri_music_creator) }

        closeButton.setOnClickListener {
            mMusicPlayerHelper.buttonSoundAsync()
            this.dismiss()
        }
    }

    private fun openUri(resId: Int) {
        mMusicPlayerHelper.lowSoundAsync()
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(getString(resId))
        })
    }



    override fun dismiss() {
        super.dismiss()
        active = false
    }

    companion object {
        @JvmStatic
        fun newInstance(fragmentManager: FragmentManager): InformationDialog {
            return InformationDialog().apply { show(fragmentManager, TAG) }
        }
    }
}