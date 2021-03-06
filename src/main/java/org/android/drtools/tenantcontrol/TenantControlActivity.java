package org.android.drtools.tenantcontrol;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.work.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class TenantControlActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavController navController;
    private View mMainContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        mDrawerList.setNavigationItemSelectedListener((menuItem) -> {
            mDrawerLayout.closeDrawer(mDrawerList);
            selectItem(menuItem);
            return true;
        });

        createNotificationChannel();

        mMainContainer = mDrawerLayout.findViewById(R.id.main_contain_id);

        mDrawerToggle = new MyDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.app_name, R.string.app_name, mMainContainer, mDrawerList);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        navController = Navigation.findNavController(this, R.id.main_contain_id);
        navController.addOnDestinationChangedListener(this);


        SharedPreferences mainPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean scheduleOn = mainPref.getBoolean(SetPrefsFragment.SCHEDULE_ON, false);
        WorkManager wm = WorkManager.getInstance(getApplicationContext());
        wm.cancelAllWorkByTag("periodic_work");
        if (scheduleOn) {
            String url = mainPref.getString("url_preference", Commons.TENANT_URL);
            int time = mainPref.getInt(SetPrefsFragment.TIME_SCHEDULE, Commons.SCHEDULE_TIME);
            Log.i(MyWorker.TAG, "Minutes = " + time);

            Data data = new Data.Builder()
                    .putString(MyWorker.URI, url)
                    .build();
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, time, TimeUnit.MINUTES)
                    .addTag("periodic_work")
                    .setInputData(data)
                    .setConstraints(constraints)
                    .setInitialDelay(time, TimeUnit.MINUTES)
                    .build();

            wm.enqueue(periodicWorkRequest);
        }
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(MyWorker.CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setShowBadge(true);
        channel.enableVibration(true);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }


    private void selectItem(MenuItem menuItem) {


        switch (menuItem.getItemId()) {
            case R.id.menu_main_list:
                navController.navigate(R.id.main_list);
                break;

            case R.id.menu_settings:
                navController.navigate(R.id.set_prefs);
                break;

            case R.id.menu_exit:
                finish();
                break;
            case R.id.menu_about:
                break;

        }

    }

    @Override
    public void onBackPressed() {
        NavDestination dest = navController.getCurrentDestination();
        int cid = -100;
        if (null != dest) {
            cid = dest.getId();
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (R.id.set_prefs == cid) {
            navController.popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onDestinationChanged(@NonNull @NotNull NavController controller, @NonNull @NotNull NavDestination destination, @Nullable @org.jetbrains.annotations.Nullable Bundle arguments) {

        int UNSPEC = -1000;

        int dId = destination.getId();

        int tId = UNSPEC;
        switch(dId) {
            case R.id.main_list:
                tId = R.id.menu_main_list;
                break;
            case R.id.set_prefs:
                tId = R.id.menu_settings;
        }

        if (tId != UNSPEC) {
            Menu m = mDrawerList.getMenu();
            for (int i = 0; i < m.size(); i++) {
                MenuItem it = m.getItem(i);
                if (it.isChecked()) {
                    it.setChecked(false);
                    it.setEnabled(true);
                }
            }

            MenuItem menuItem = m.findItem(tId);
            menuItem.setChecked(true);
            menuItem.setEnabled(false);
        }
    }
}
