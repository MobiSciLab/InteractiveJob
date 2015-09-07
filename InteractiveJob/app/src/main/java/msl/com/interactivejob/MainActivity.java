package msl.com.interactivejob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import msl.com.httpclient.HttpRegGeoHelper;
import msl.com.httpclient.MSConfig;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String PROFILE = "PROFILE";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section1);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        try {
            queryGeneralInfo();
            mProfile = new JSONObject(getIntent().getStringExtra(PROFILE));
            mLoginToken = mProfile.getString("login_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    JobsFragment mJobsFragment = null;
    SubscriptionFragment mSubscriptionFragment = null;
    ProfileFragment mProfileFrament = null;
    private static final int JOBS_POS = 0;
    private static final int PROFILE_POS = 1;
    private static final int SUBSCRIPTION_POS = 2;
    private static final int LOGOUT_POS = 3;
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the jobs_menu content by replacing fragments
        Log.i(TAG, "Selected: " + position);
        if(mJobsFragment == null) {
            mJobsFragment = new JobsFragment();
        }
        if(mSubscriptionFragment == null) {
            mSubscriptionFragment = new SubscriptionFragment();
        }
        if(mProfileFrament == null) {
            mProfileFrament = new ProfileFragment();
        }

        Fragment activeFragment = mJobsFragment;
        if(position == JOBS_POS) {
            activeFragment = mJobsFragment;
            mTitle = getString(R.string.title_section1);
        } else if(position == PROFILE_POS) {
            activeFragment = mProfileFrament;
            mTitle = getString(R.string.title_section2);
        } else if(position == SUBSCRIPTION_POS) {
            activeFragment = mSubscriptionFragment;
            mTitle = getString(R.string.title_section3);
        } else if(position == LOGOUT_POS) {
            if(MSConfig.DEBUG) {
                Log.i(TAG, "Selected: Logout");
            }
            //Clear password
            SharedPreferences sharedPref = getSharedPreferences(LoginActivity.PREFS_KEY_LOGIN, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(LoginActivity.KEYWORD_KEEP_LOGGED_IN, false);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();


            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, activeFragment)
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    //entry point for singleTop activities which already run somewhere else in the stack and therefor can't call onCreate(). From activities lifecycle point of view it's therefore needed to call onPause()before onNewIntent()
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (MSConfig.DEBUG) {
            Log.i(TAG, "call onNewIntent");
        }
        setIntent(intent);
        if(mJobsFragment != null) {
            mJobsFragment.handleIntent(intent);
        } else {
            Log.i(TAG, "MainFragment is NULL.......");
        }
    }


    private static final int TYPE_MAX = 3;
    protected static final int TYPE_CATEGORIES = 0;
    protected static final int TYPE_LOCATIONS = 1;
    protected static final int TYPE_LEVELS = 2;

    JSONArray[] mJsonData = new JSONArray[TYPE_MAX];
    private void queryGeneralInfo() {
        new HttpRegGeoHelper(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if(data == null) {
                    Toast.makeText(MainActivity.this, "Network or Server error!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject((String)data);
                        mJsonData[TYPE_CATEGORIES] = jsonObject.getJSONObject("data").getJSONArray("categories");
                        mJsonData[TYPE_LOCATIONS] = jsonObject.getJSONObject("data").getJSONArray("locations");
                        mJsonData[TYPE_LEVELS] = jsonObject.getJSONObject("data").getJSONArray("job_levels");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, (String) data);

            }
        }).getGeneralConfig();
    }

    JSONObject mProfile;
    protected synchronized JSONObject getPersonalInfo() {
        return mProfile;
    }
    protected synchronized JSONArray[] getGeneralInfo() {
        return mJsonData;
    }

    String mLoginToken;
    protected synchronized String getLoginToken() {
        return mLoginToken;
    }

    protected synchronized void setLoginToken(String token) {
        mLoginToken = token;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Activity resutl.......1");
        if(resultCode == Activity.RESULT_OK && data != null && data.hasExtra(CameraActivity.COMPANY_NAME)) {
            Log.i(TAG, "Activity resutl.......");
            String keyword = data.getStringExtra(CameraActivity.COMPANY_NAME);
            mJobsFragment.doSearchByKeyword(keyword);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
