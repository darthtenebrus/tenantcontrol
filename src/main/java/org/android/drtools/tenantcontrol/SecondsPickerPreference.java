package org.android.drtools.tenantcontrol;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;

public class SecondsPickerPreference extends DialogPreference {

    public SecondsPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public SecondsPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SecondsPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
