package com.nepal.tia.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nepal.tia.R;
import com.nepal.tia.event.OnItemActionListener;
import com.nepal.tia.model.TrackingInfo;
import com.nepal.tia.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class AdminTrackingAdapter extends RecyclerView.Adapter<AdminTrackingAdapter.ViewHolder> {

    private final List<TrackingInfo> dataList = new ArrayList<>();
    private final OnItemActionListener<TrackingInfo> listener;

    public AdminTrackingAdapter(final OnItemActionListener<TrackingInfo> listener) {
        super();
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tracking_admin, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TrackingInfo trackingInfo = dataList.get(position);

        holder.name.setText(trackingInfo.getUser().getName());
        holder.email.setText(trackingInfo.getUser().getEmail());
        holder.contact.setText(trackingInfo.getUser().getContact());
        holder.startDate.setText(DateUtils.format(trackingInfo.getStartDate()));
        holder.endDate.setText(DateUtils.format(trackingInfo.getEndDate()));

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemEditAction(v, position, trackingInfo);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemDeleteAction(v, position, trackingInfo);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(position).getId();
    }

    public void update(TrackingInfo trackingInfo) {
        if (trackingInfo == null) return;

        int changedIndex = 0;
        boolean hasItem = false;
        for (int i = 0; i < dataList.size(); i++) {
            TrackingInfo serviceInfo1 = dataList.get(i);
            if (serviceInfo1.getId() == trackingInfo.getId()) {
                serviceInfo1.setId(trackingInfo.getId());
                serviceInfo1.setUser(trackingInfo.getUser());
                serviceInfo1.setEndDate(trackingInfo.getEndDate());
                serviceInfo1.setStartDate(trackingInfo.getStartDate());
                changedIndex = i;
                hasItem = true;
                break;
            }
        }
        if (hasItem) {
            notifyItemChanged(changedIndex);
        } else {
            addData(trackingInfo);
        }
    }

    public void addData(TrackingInfo trackingInfo) {
        if (trackingInfo == null) return;

        this.dataList.add(0, trackingInfo);
        notifyItemInserted(0);
    }

    public void removeData(TrackingInfo trackingInfo) {
        if (trackingInfo == null) return;

        boolean isRemoved = false;
        int index = 0;
        for (int i = 0; i < dataList.size(); i++) {
            TrackingInfo serviceInfo1 = dataList.get(i);
            if (serviceInfo1.getId() == trackingInfo.getId()) {
                dataList.remove(serviceInfo1);
                isRemoved = true;
                index = i;
                break;
            }
        }

        if (isRemoved) {
            notifyItemRemoved(index);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView name;
        AppCompatTextView email;
        AppCompatTextView contact;
        AppCompatTextView startDate;
        AppCompatTextView endDate;
        AppCompatButton editButton;
        AppCompatButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            contact = itemView.findViewById(R.id.contact);
            startDate = itemView.findViewById(R.id.tracking_start_date);
            endDate = itemView.findViewById(R.id.tracking_end_date);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);

        }
    }
}
