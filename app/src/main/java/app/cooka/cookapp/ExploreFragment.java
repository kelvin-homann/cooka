package app.cooka.cookapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class ExploreFragment extends Fragment {

    //Represents parent activity
    private OnFragmentInteractionListener mListener;
    private Toolbar toolbar;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.fragment_explore, container, false);

        //Get the toolbar from the inflated layout and assign the menu
        toolbar = fragmentView.findViewById(R.id.explore_toolbar);
        toolbar.inflateMenu(R.menu.explore_toolbar);

        //Return the inflated view
        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof MainActivity)) {
            throw new RuntimeException(context.toString()
                    + " should only be attached to MainActivity.");
        }

        mListener = (OnFragmentInteractionListener) context;

        ((MainActivity)context).setSupportActionBar(toolbar);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
