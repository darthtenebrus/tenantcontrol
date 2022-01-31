package org.android.drtools.tenantcontrol;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public final class VisualSyncTask extends DefaultSyncTask {



    private static class InstanceHolder {
        private static VisualSyncTask INSTANCE = new VisualSyncTask(TaskType.EXECUTABLE);
    }

    interface FinishListener {
        void onFinish(List<MyResViewAdapter.DataHolder> data);
    }

    private FinishListener onFinishListener;

    public VisualSyncTask setOnFinishListener(FinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
        return VisualSyncTask.this;
    }

    private VisualSyncTask(TaskType tType) {
        super(tType);
    }

    public static VisualSyncTask getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected void onUiReturn(JsonNode response) {

        List<MyResViewAdapter.DataHolder> items = MyResViewAdapter.parseData(response);
        if (null != items && null != onFinishListener) {
            onFinishListener.onFinish(items);
        }

    }
}
