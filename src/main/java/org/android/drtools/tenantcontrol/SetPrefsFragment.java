package org.android.drtools.tenantcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class SetPrefsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TIME_SCHEDULE = "pref_time_schedule";
    public static final String SCHEDULE_ON = "pref_schedule_on";

    private static final String DIALOG_FRAGMENT_TAG = "SecondsPickerPreference";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
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
    public void onDisplayPreferenceDialog(Preference preference) {
        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        if (preference instanceof SecondsPickerPreference) {
            final DialogFragment f = SecondsPickerPreferenceDialog.newInstance(
                    getContext(),
                    preference.getKey(),
                    getParentFragmentManager()
            );
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
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
                int timeSchedule = sharedPreferences.getInt(TIME_SCHEDULE, Commons.SCHEDULE_TIME);

                b.putString("url_preference", url);
                b.putBoolean(SCHEDULE_ON, schedOn);
                b.putInt(TIME_SCHEDULE, timeSchedule);

                getParentFragmentManager().setFragmentResult("settings", b);
        }
    }
}
