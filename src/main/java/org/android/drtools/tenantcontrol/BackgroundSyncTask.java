package org.android.drtools.tenantcontrol;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public final class BackgroundSyncTask extends DefaultSyncTask {

    private static class InstanceHolder {
        private static BackgroundSyncTask INSTANCE = new BackgroundSyncTask(TaskType.WORKER);
    }


    interface ProcessListener {
        void onProcess(List<MyResViewAdapter.DataHolder> data);
    }

    private ProcessListener onProcessListener;

    public BackgroundSyncTask setOnProcessListener(ProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
        return BackgroundSyncTask.this;
    }

    private BackgroundSyncTask(TaskType tType) {
        super(tType);
    }

    public static BackgroundSyncTask getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected void onUiReturn(JsonNode response) {
        List<MyResViewAdapter.DataHolder> items = MyResViewAdapter.parseData(response);
        if (null != items && null != onProcessListener) {
            onProcessListener.onProcess(items);
        }
    }
}
