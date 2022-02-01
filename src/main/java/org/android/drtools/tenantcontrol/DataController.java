package org.android.drtools.tenantcontrol;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public final class DataController {

    private DataController() {}

    private static class LiveDataHolder {
        static MutableLiveData<List<MyResViewAdapter.DataHolder>> INSTANCE = new MutableLiveData<>();
    }

    public static MutableLiveData<List<MyResViewAdapter.DataHolder>> getDataInstance() {
        return LiveDataHolder.INSTANCE;
    }
}
