package org.android.drtools.tenantcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeContainer;
    private DefaultSyncTask mSyncTask = null;
    private SharedPreferences mPref;
    private MyResViewAdapter mSmpl;
    private RecyclerView mResListView;
    private DefaultSyncTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("settings",
                this,
                (key, bundle) -> {
                    Log.i(AbstractSyncTask.TAG, "Data received");
                    boolean result = bundle.getBoolean(SetPrefsFragment.SCHEDULE_ON, false);
                    if (result) {
                        int delay = bundle.getInt(SetPrefsFragment.TIME_SCHEDULE, 0);
                        Log.i(AbstractSyncTask.TAG, "Seconds = " + delay);
                        if (0 != delay) {
                            long delayLong = TimeUnit.MILLISECONDS.convert(delay, TimeUnit.SECONDS);
                            Log.i(AbstractSyncTask.TAG, "Milliseconds = " + delayLong);
                            String url = bundle.getString("url_preference", Commons.TENANT_URL);
                            BackgroundSyncTask.getInstance()
                                    .execute(url, delayLong, delayLong);
                        } else {
                            BackgroundSyncTask.getInstance().cancelWorker();
                        }
                    } else {
                        BackgroundSyncTask.getInstance().cancelWorker();
                    }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_list, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        mSwipeContainer = view.findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(this);

        mSmpl = new MyResViewAdapter();
        mResListView = mSwipeContainer.findViewById(R.id.main_host_list);
        mResListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mResListView.setHasFixedSize(false);
        mResListView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mResListView.setAdapter(mSmpl);

        if (!mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(true);
        }

        mTask = VisualSyncTask.getInstance()
                        .setOnFinishListener(items -> {
                            if (mSwipeContainer.isRefreshing()) {
                                mSwipeContainer.setRefreshing(false);
                            }

                            if (null != items) {
                                mSmpl.refresh(items);
                            }
                        });

        doRefresh();

    }

    @Override
    public void onDestroyView() {
        nullifyAll();
        super.onDestroyView();
    }

    private void nullifyAll() {
        mPref = null;
        mSwipeContainer = null;
        mSmpl = null;
        mResListView = null;
        mTask = null;
    }

    public void doRefresh() {
        String url = mPref.getString("url_preference", Commons.TENANT_URL);
        if (!"".equals(url)) {
            mTask.execute(url);
        } else {
            if (mSwipeContainer.isRefreshing()) {
                mSwipeContainer.setRefreshing(false);
            }
        }
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }
}
