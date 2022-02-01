package org.android.drtools.tenantcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.*;

import java.util.concurrent.TimeUnit;

public class MainListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeContainer;

    private SharedPreferences mPref;
    private MyResViewAdapter mSmpl;
    private RecyclerView mResListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("settings",
                this,
                (key, bundle) -> {
                    Log.i(MyWorker.TAG, "Data received");
                    WorkManager wm = WorkManager.getInstance(getContext().getApplicationContext());
                    wm.cancelAllWorkByTag("periodic_work");
                    boolean result = bundle.getBoolean(SetPrefsFragment.SCHEDULE_ON, false);
                    if (result) {
                        int delay = bundle.getInt(SetPrefsFragment.TIME_SCHEDULE, 0);
                        Log.i(MyWorker.TAG, "Minutes = " + delay);
                        if (0 != delay) {

                            String url = bundle.getString("url_preference", Commons.TENANT_URL);
                            Data data = new Data.Builder()
                                    .putString(MyWorker.URI, url)
                                    .build();
                            Constraints constraints = new Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build();

                            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, delay, TimeUnit.MINUTES)
                                    .addTag("periodic_work")
                                    .setInputData(data)
                                    .setConstraints(constraints)
                                    .setInitialDelay(delay, TimeUnit.MINUTES)
                                    .build();

                            wm.enqueue(periodicWorkRequest);
                        }
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

        DataController.getDataInstance()
                .observe(getViewLifecycleOwner(), items -> {
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
    }

    public void doRefresh() {
        String url = mPref.getString("url_preference", Commons.TENANT_URL);
        if (!"".equals(url)) {
            Data data = new Data.Builder()
                    .putString(MyWorker.URI, url)
                    .build();
            OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .addTag("simple_work")
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(
                    getContext().getApplicationContext()
            ).enqueue(simpleRequest);
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
