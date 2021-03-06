package org.android.drtools.tenantcontrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.*;
import com.fasterxml.jackson.databind.JsonNode;


import java.util.ArrayList;
import java.util.List;

public class MyResViewAdapter extends RecyclerView.Adapter<MyResViewAdapter.ViewHolder> implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        MyResViewAdapter.ViewHolder h = (MyResViewAdapter.ViewHolder) v.getTag(R.string.all_ok);
        if (null != h) {
            View btx = h.getBottomView();
            int vis = btx.getVisibility();
            Transition set = TransitionInflater.from(v.getContext().getApplicationContext())
                    .inflateTransition(R.transition.main_trans);

            TransitionManager.beginDelayedTransition((ViewGroup) v.getRootView(), set);
            h.getBottomView().setVisibility(vis == View.GONE ? View.VISIBLE : View.GONE);
        }
    }

    public static class DataHolder {
        private Boolean hostStatus;
        private String hostType;
        private String hostName;
        private String version;
        private Boolean schemaExists;
        private Boolean usersExist;


        public Boolean getHostStatus() {
            return hostStatus;
        }

        public void setHostStatus(Boolean hostStatus) {
            this.hostStatus = hostStatus;
        }

        public String getHostType() {
            return hostType;
        }

        public void setHostType(String hostType) {
            this.hostType = hostType;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Boolean getSchemaExists() {
            return schemaExists;
        }

        public void setSchemaExists(Boolean schemaExists) {
            this.schemaExists = schemaExists;
        }

        public Boolean getUsersExist() {
            return usersExist;
        }

        public void setUsersExist(Boolean usersExist) {
            this.usersExist = usersExist;
        }
    }


    public static List<DataHolder> parseData(JsonNode response) {
        if (null != response) {
            JsonNode data = response.hasNonNull("data") ? response.get("data") : null;
            if (null != data && data.isArray()) {
                List<DataHolder> items = new ArrayList<>();
                for (JsonNode item : data) {
                    DataHolder dataItem = new MyResViewAdapter.DataHolder();
                    if (item.hasNonNull("id")) {
                        dataItem.setHostName(item.get("id").asText());
                    } else {
                        continue;
                    }

                    boolean isHost = false;
                    if (item.hasNonNull("type")) {
                        String hType = item.get("type").asText();
                        dataItem.setHostType(hType);
                        if ("valo-servers".equals(hType)) {
                            dataItem.setHostStatus(true);
                            isHost = true;
                        }
                    } else {
                        continue;
                    }

                    if (item.hasNonNull("attributes")) {
                        JsonNode attribs = item.get("attributes");
                        if(!isHost) {
                            if (attribs.hasNonNull("DB-update")) {
                                String DBUpdate = attribs.get("DB-update").asText();
                                dataItem.setHostStatus("COMPLETED".equals(DBUpdate));
                            } else {
                                dataItem.setHostStatus(false);
                            }
                        }

                        dataItem.setUsersExist(attribs.hasNonNull("users") &&
                                "EXISTS".equalsIgnoreCase(attribs.get("users").asText()));

                        dataItem.setSchemaExists(attribs.hasNonNull("schema_exists") &&
                                attribs.get("schema_exists").asBoolean());

                        if (attribs.hasNonNull("version")) {
                            dataItem.setVersion(attribs.get("version").asText());
                        }
                    }
                    items.add(dataItem);
                }
                return items;
            }
        }
        return null;
    }

    private List<DataHolder> mList = new ArrayList<>();

    public void refresh(List<DataHolder> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();

    }

    @Override
    public MyResViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.host_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyResViewAdapter.ViewHolder holder, int position) {

        DataHolder dataHolder = mList.get(position);
        holder.getItemName().setText(dataHolder.getHostName());
        holder.getItemType().setText(dataHolder.getHostType());
        holder.getItemVersion().setText(dataHolder.getVersion());
        Boolean status = dataHolder.getHostStatus();
        Boolean schema = dataHolder.getSchemaExists();
        Boolean user = dataHolder.getUsersExist();

        View iv = holder.getIv();
        iv.setBackgroundResource(status ? R.drawable.status : R.drawable.status_red);
        TextView txtview = holder.getBottomView()
                .findViewById(R.id.in_work_view);
        txtview.setText(status ? R.string.str_oper : R.string.str_not_oper);
        ImageView imgview = holder.getBottomView()
                .findViewById(R.id.in_work_image_view);
        imgview.setImageResource(status ? R.drawable.ic_green : R.drawable.ic_red);

        imgview = holder.getBottomView()
                .findViewById(R.id.schema_image_view);
        imgview.setImageResource(schema ? R.drawable.ic_green : R.drawable.ic_red);

        imgview = holder.getBottomView()
                .findViewById(R.id.user_image_view);
        imgview.setImageResource(user ? R.drawable.ic_green : R.drawable.ic_red);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemVersion;
        private final View iv;
        private final TextView itemName;
        private final TextView itemType;
        private final View bottomView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(MyResViewAdapter.this);
            itemView.setTag(R.string.all_ok, ViewHolder.this);
            iv = itemView.findViewById(R.id.item_host_status);
            itemName = itemView.findViewById(R.id.item_name_text);
            itemType = itemView.findViewById(R.id.item_type_text);
            itemVersion = itemView.findViewById(R.id.item_version_text);
            bottomView = itemView.findViewById(R.id.bottom_view);
        }


        public View getIv() {
            return iv;
        }

        public TextView getItemName() {
            return itemName;
        }

        public TextView getItemType() {
            return itemType;
        }

        public TextView getItemVersion() {
            return itemVersion;
        }

        public View getBottomView() {
            return bottomView;
        }
    }
}
