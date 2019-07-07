package sh.now.alecsisduarte.who_says.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import sh.now.alecsisduarte.who_says.enums.GridSize
import sh.now.alecsisduarte.who_says.R

private val GRIDSIZE: String = "GRIDSIZE"
private val SOUNDON: String = "SOUNDON"

class GameBoardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var gridSize: GridSize? = GridSize.NORMAL
    private var soundOn: Boolean? = true
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gridSize = it.getInt(GRIDSIZE, GridSize.NORMAL as Int) as GridSize
            soundOn = it.getBoolean(SOUNDON, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.normal_board, container, false)

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onSimonButtonClick(view: View) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
        fun onFragmentStart()
        fun onFragmentStop()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * the Game Board fragment using the provided parameters.
         *
         * @param gridSize Sets the size of the grid.
         * @param soundOn Enables or disables sound.
         * @return A new instance of fragment GameBoardFragment.
         */
        @JvmStatic
        fun newInstance(gridSize: GridSize = GridSize.NORMAL, soundOn: Boolean = true) =
            GameBoardFragment().apply {
                arguments = Bundle().apply {
                    putInt(GRIDSIZE, gridSize.ordinal)
                    putBoolean(SOUNDON, soundOn)
                }
            }
    }
}
