package org.android.drtools.tenantcontrol;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Calendar;
import java.util.TimeZone;


public class SetPrefsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TIME_SCHEDULE = "pref_time_schedule";
    public static final String SCHEDULE_ON = "pref_schedule_on";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        findPreference(TIME_SCHEDULE).setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference pref) {

        final Calendar dd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        final long lMillis = pref.getSharedPreferences().getLong(TIME_SCHEDULE, Commons.SCHEDULE_TIME);
        dd.setTimeInMillis(lMillis);
        int lHour = dd.get(Calendar.HOUR_OF_DAY);
        int lMinute = dd.get(Calendar.MINUTE);
        (new TimePickerDialog(getContext(),
                (dpv, hourOfDay, minute) -> {

                    dd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    dd.set(Calendar.MINUTE, minute);

                    SharedPreferences.Editor ed = pref.getSharedPreferences().edit();
                    ed.putLong(TIME_SCHEDULE, dd.getTimeInMillis());
                    ed.commit();

                }, lHour, lMinute, true)).show();


        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Bundle b = new Bundle();

        switch(key) {
            case SCHEDULE_ON:
            case TIME_SCHEDULE:
            case "url_preference":

                boolean schedOn = sharedPreferences.getBoolean(SCHEDULE_ON, false);
                String url = sharedPreferences.getString("url_preference", Commons.TENANT_URL);
                long timeSchedule = sharedPreferences.getLong(TIME_SCHEDULE, Commons.SCHEDULE_TIME);

                b.putString("url_preference", url);
                b.putBoolean(SCHEDULE_ON, schedOn);
                b.putLong(TIME_SCHEDULE, timeSchedule);

                getParentFragmentManager().setFragmentResult("settings", b);
        }
    }
}
