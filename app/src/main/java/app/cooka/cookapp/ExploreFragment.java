package app.cooka.cookapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ExploreFragment extends Fragment {

    //Represents parent activity
    private OnFragmentInteractionListener mListener;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private View fragmentView;
    private  ViewPagerAdapter adapter;

    private ExploreHomeFragment exploreHomeFragment;
    private ExploreTrendingFragment exploreTrendingFragment;
    private ExploreFollowingFragment exploreFollowingFragment;


    public ExploreFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        exploreHomeFragment = new ExploreHomeFragment();
        exploreTrendingFragment = new ExploreTrendingFragment();
        exploreFollowingFragment = new ExploreFollowingFragment();


        //Inflate the fragment view
        fragmentView = inflater.inflate(R.layout.fragment_explore, container, false);
        viewPager = (ViewPager)fragmentView.findViewById(R.id.viewPager_id);

        adapter = new ViewPagerAdapter(getChildFragmentManager());

        viewPager.setOffscreenPageLimit(4);

        //Get the toolbar from the inflated layout and assign the menu
        toolbar = fragmentView.findViewById(R.id.explore_toolbar);
        toolbar.inflateMenu(R.menu.explore_toolbar);

        tabLayout = fragmentView.findViewById(R.id.tablayout_id);
        viewPager = fragmentView.findViewById(R.id.viewPager_id);

        //Adding Fragments
        adapter.addFragment(exploreHomeFragment,"Home");
        adapter.addFragment(exploreTrendingFragment,"Trending");
        adapter.addFragment(exploreFollowingFragment,"Following");


        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        //Setup the MenuItem click listener to handle toolbar menu actions
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onToolbarMenuItemClicked(menuItem);
            }
        });

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

    //This method is called when a menu item from the toolbar is selected
    private boolean onToolbarMenuItemClicked(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_explore_search:
                //Search action
                Toast.makeText(this.getActivity(), R.string.action_search, Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_settings:
                //Settings action
                Intent intent_settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent_settings);
                return true;
            case R.id.action_profile:
                //Starting the Profile Activity
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }
}
