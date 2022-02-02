package org.android.drtools.tenantcontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.Result;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nazgul on 20.04.15.
 */
public class MyWorker extends Worker {


    public static final String TAG = "MyWorker";
    public static final String URI = "uri";
    public static final String CHANNEL_ID = "org.android.drtools.tenantcontrol.notifications";

    private static final AtomicInteger counter = new AtomicInteger();

    public static int nextValue() {
        return counter.getAndIncrement();
    }

    public MyWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.e(TAG, "Thread started");

        String uri = getInputData().getString(URI);
        try {
            JsonNode result = buildJson(uri);

            if (null != result) {
                List<MyResViewAdapter.DataHolder> data = MyResViewAdapter.parseData(result);

                MutableLiveData<List<MyResViewAdapter.DataHolder>> ld = DataController.getDataInstance();
                if (!ld.hasActiveObservers()) {
                    sendNotification(data);
                } else {
                    ld.postValue(data);
                }
                return Result.success();

            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return Result.failure();

    }

    private void sendNotification(List<MyResViewAdapter.DataHolder> data) {

        Optional<MyResViewAdapter.DataHolder> opt = data.stream()
                .filter(d -> !d.getHostStatus()).findFirst();
        String content = null;
        if (!opt.isPresent()) {
            content = getApplicationContext().getResources().getString(R.string.all_ok);
        } else {
            content = String.format(
                    getApplicationContext().getResources().getString(R.string.all_not_ok),
                    opt.get().getHostName());
        }


        NotificationCompat.Builder lBuilder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0,
                (new Intent(getApplicationContext(), TenantControlActivity.class))
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

        lBuilder.setSmallIcon(R.drawable.ic_stat_cloud_done)
                .setContentIntent(contentIntent)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.str_notif))
                .setContentText(content)
                .setTicker(content)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setOngoing(false);
        NotificationManagerCompat.from(
                getApplicationContext()
        ).notify(nextValue(), lBuilder.build());

    }


    private JsonNode buildJson(String uri) throws Exception {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri)
                .method("GET", null)
                .header("Accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            if (null != body) {
                ObjectMapper mapper = new ObjectMapper();
                JsonParser parser = mapper.getFactory()
                        .createParser(body.byteStream());
                return parser.readValueAsTree();
            }
        }
        return null;
    }

}
