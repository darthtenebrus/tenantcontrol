package org.android.drtools.tenantcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceDialogFragmentCompat;

public class SecondsPickerPreferenceDialog extends PreferenceDialogFragmentCompat {

    private MyNumberPicker picker;
    private String key;

    public static SecondsPickerPreferenceDialog newInstance(Context context, String key, FragmentManager fm) {
        FragmentFactory factory = fm.getFragmentFactory();
        SecondsPickerPreferenceDialog f = (SecondsPickerPreferenceDialog) factory.instantiate(context.getClassLoader(),
                SecondsPickerPreferenceDialog.class.getName());
        Bundle args = new Bundle();
        args.setClassLoader(f.getClass().getClassLoader());
        args.putString("key", key);
        f.setArguments(args);
        return f;

    }


    @Override
    protected View onCreateDialogView(Context ctx) {
        View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_layout, null);
        picker = dialogView.findViewById(R.id.num_picker);
        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Bundle b = getArguments();
        key = b.getString("key");

        int val = getPreference().getSharedPreferences().getInt(key, Commons.SCHEDULE_TIME);
        picker.setValue(val);
        Log.i("AbstractSyncTask", "VAL = " + val);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            picker.clearFocus();
            SharedPreferences.Editor ed = getPreference()
                    .getSharedPreferences().edit();
            ed.putInt(key, picker.getValue());
            ed.commit();

        }

    }
}
