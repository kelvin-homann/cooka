package app.cooka.cookapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class CookbookFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private CookbookRecipesFragment cookbookRecipesFragment;
    private CookbookCollectionsFragment cookbookCollectionsFragment;

    public CookbookFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cookbookRecipesFragment = new CookbookRecipesFragment();
        cookbookCollectionsFragment = new CookbookCollectionsFragment();

        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.fragment_cookbook, container, false);
        viewPager = (ViewPager)fragmentView.findViewById(R.id.viewPager_id);


        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        viewPager.setOffscreenPageLimit(2);

        //Get the toolbar from the inflated layout and assign the menu
        toolbar = fragmentView.findViewById(R.id.cookbook_toolbar);
        toolbar.inflateMenu(R.menu.cookbook_toolbar);

        tabLayout = fragmentView.findViewById(R.id.tablayout_id);
        viewPager = fragmentView.findViewById(R.id.viewPager_id);

        //Adding Fragments
        adapter.addFragment(cookbookRecipesFragment,"Recipes");
        adapter.addFragment(cookbookCollectionsFragment,"Collections");
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
