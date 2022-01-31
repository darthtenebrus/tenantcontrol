package org.android.drtools.tenantcontrol;

import android.net.ParseException;
import android.os.RemoteException;
import android.util.Log;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class DefaultSyncTask extends AbstractSyncTask<String, JsonNode> {

    private static final String TAG = "AsyncTask";

    protected DefaultSyncTask(TaskType tType) {
        super(tType);
    }

    @Override
    protected JsonNode doOnThread(String uri) {

        Log.e(TAG, "Thread started");

        try {
            return buildJson(uri);

        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);

        } catch (final SAXException e) {
            Log.e(TAG, "SAXException", e);
        } catch (final ParseException | RemoteException e) {
            Log.e(TAG, "ParseException", e);
        } catch (Exception e) {
            Log.e(TAG,"", e);
        }
        return null;
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
