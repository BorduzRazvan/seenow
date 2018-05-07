package work.seenow.seenow;

import work.seenow.seenow.Utils.SQLiteHandler;
import work.seenow.seenow.Utils.SessionManager;

import java.util.HashMap;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static String PACKAGE_NAME;

    private SQLiteHandler db;
    private SessionManager session;
    private FragmentPagerAdapter adapterViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        setContentView(R.layout.activity_main);
        ViewPager vpPager = (ViewPager) findViewById(R.id.viewpager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        tabLayout.setupWithViewPager(vpPager);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_camera_normal);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return FeedActivity.newInstance(0, "Feed");
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return GalleryActivity.newInstance(1, "Gallery");
                case 2: // Fragment # 1 - This will show SecondFragment
                    return CameraActivity.newInstance(2, "Camera");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Feed";
                case 1: return "Gallery";
            }
            return "";
        }
    }

}