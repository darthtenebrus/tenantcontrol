package org.android.drtools.tenantcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nazgul on 20.04.15.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSyncTask<E, T> {

    public static final String TAG = "AbstractSyncTask";
    private static final int ASYNC_RESULT_OK = 0;
    private final TaskType tType;
    private Timer mTimer;

    public enum TaskType {
        EXECUTABLE,
        WORKER
    }


    private final Handler uiThreadHandler;

    protected AbstractSyncTask(TaskType tType) {

        this.tType = tType;

        this.uiThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                    case ASYNC_RESULT_OK:
                        T response = (T) msg.obj;
                        onUiReturn(response);
                        break;
                }

            }
        };
    }

    
    protected abstract void onUiReturn(T response);

    protected abstract T doOnThread(E uri);

    public void execute(E uri) {
        execute(uri, null, null);
    }

    public void execute(E uri, Long delay, Long period) {

        Runnable r = () -> {
            try {
                if (Thread.interrupted()) {
                    throw new Exception();
                }
                T ret = doOnThread(uri);
                Message message = uiThreadHandler.obtainMessage(ASYNC_RESULT_OK, ret);
                uiThreadHandler.sendMessage(message);
            } catch (Exception e) {
            }
        };

        if (TaskType.EXECUTABLE.equals(tType)) {
            Thread t = new Thread(r);
            t.start();
        } else if (TaskType.WORKER.equals(tType) && null != delay && null != period) {
            if (null != mTimer) {
                mTimer.cancel();
                mTimer = null;
                Log.i(TAG, "Prev Timer canceled");

            }

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    r.run();
                    Log.i(TAG, "Background job run");
                }
            }, delay, period);
            Log.i(TAG, "Timer started");
        }
    }

    public boolean cancelWorker() {
        if(TaskType.WORKER.equals(tType) && null != mTimer) {
            mTimer.cancel();
            mTimer = null;
            Log.i(TAG, "Timer canceled");
            return true;
        } else {
            Log.i(TAG, "Timer already canceled");
            return false;
        }
    }
}
