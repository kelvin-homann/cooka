package app.cooka.cookapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import app.cooka.cookapp.view.LoadingScreenView;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final String VIEW_EXPLORE = "android.intent.action.VIEW_EXPLORE";
    public static final String VIEW_COLLECTION = "android.intent.action.VIEW_COLLECTION";
    public static final String VIEW_GROCERYLIST = "android.intent.action.VIEW_GROCERYLIST";

    //Bottom navigation bar ui component
    private BottomNavigationView navigationView;

    //Main fragments for the different screens of the app
    private ExploreFragment exploreFragment;
    private CookbookFragment cookbookFragment;
    private PlanFragment planFragment;
    private CookFragment cookFragment;

    //Generic loading screen
    private LoadingScreenView loadingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent callIntent = getIntent();
        final String intentAction = callIntent.getAction();

        //Get reference to bottom navigation bar
        navigationView = findViewById(R.id.navigation);

        //Add the item selected listener that calls onNavigationItemSelected to switch the fragment
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return MainActivity.this.onNavigationItemSelected(menuItem);
            }
        });

        //Init fragments
        exploreFragment = new ExploreFragment();
        cookbookFragment = new CookbookFragment();
        planFragment = new PlanFragment();
        cookFragment = new CookFragment();

        // load intended fragment
        if(intentAction.equalsIgnoreCase(VIEW_COLLECTION)) {
            loadFragment(cookbookFragment);
            navigationView.setSelectedItemId(R.id.navigation_cookbook);
        }
        else if(intentAction.equalsIgnoreCase(VIEW_GROCERYLIST)) {
            loadFragment(planFragment);
            navigationView.setSelectedItemId(R.id.navigation_plan);
        }
        // default intent action (started through android.intent.action.MAIN, etc.)
        else {
            loadFragment(exploreFragment);
            navigationView.setSelectedItemId(R.id.navigation_explore);
        }

        //Loading screen
        loadingScreen = findViewById(R.id.loading_screen);
        loadingScreen.hide();
    }

    //Change out the current fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //Switch the fragment based on selected navigation item
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_explore:
                loadFragment(exploreFragment);
                return true;
            case R.id.navigation_cookbook:
                loadFragment(cookbookFragment);
                return true;
            case R.id.navigation_plan:
                loadFragment(planFragment);
                return true;
            case R.id.navigation_cook:
                loadFragment(cookFragment);
                return true;
        }
        return false;
    }

    //Callback for fragment interaction
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

//    /**
//     * if no user is logged in it returns false, if someone is logged in it returns true
//     * @return boolean
//     */
//    public boolean isLoggedIn(){
//        return loginManager.getUserId() != 0L;
//    }
//
//    /**
//     * If the tutorial is enabled in the settings it returns true, when its disable it returns false
//     * @return boolean
//     */
//    public boolean tutorialIsEnabled(){
//        return settings.getBoolean("tutorialCheckBox", true);
//    }
}
