package app.cooka.cookapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    //Bottom navigation bar ui component
    private BottomNavigationView navigationView;

    //Main fragments for the different screens of the app
    private ExploreFragment exploreFragment;
    private CookbookFragment cookbookFragment;
    private PlanFragment planFragment;
    private CookFragment cookFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init fragments
        exploreFragment = new ExploreFragment();
        cookbookFragment = new CookbookFragment();
        planFragment = new PlanFragment();
        cookFragment = new CookFragment();

        //Load explore fragment
        loadFragment(exploreFragment);

        //Get reference to bottom navigation bar
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);

        //Add the item selected listener that calls onNavigationItemSelected to switch the fragment
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return MainActivity.this.onNavigationItemSelected(menuItem);
            }
        });
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
}
