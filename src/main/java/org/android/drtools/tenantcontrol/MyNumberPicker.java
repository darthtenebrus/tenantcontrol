package org.android.drtools.tenantcontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.NumberPicker;

public class MyNumberPicker extends NumberPicker  {

    private static final int MIN_VALUE = 30;
    private static final int MAX_VALUE = 120;
    private static final boolean WRAP_WHEEL = true;

    public MyNumberPicker(Context context) {
        super(context);
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    public MyNumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyNumberPickerStyleable);

        this.setMinValue(a.getInt(R.styleable.MyNumberPickerStyleable_minValue, MIN_VALUE));
        this.setMaxValue(a.getInt(R.styleable.MyNumberPickerStyleable_maxValue, MAX_VALUE));
        this.setWrapSelectorWheel(a.getBoolean(R.styleable.MyNumberPickerStyleable_wrapWheel, WRAP_WHEEL));
        a.recycle();

    }

}
