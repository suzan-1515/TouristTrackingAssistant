package com.nepal.tia.admin.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.nepal.tia.R;
import com.nepal.tia.model.ServiceRequest;
import com.nepal.tia.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class QueriesAdapter extends RecyclerView.Adapter<QueriesAdapter.ViewHolder> {

    private final List<ServiceRequest> dataList = new ArrayList<>();

    public QueriesAdapter() {
        super();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queries, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ServiceRequest serviceRequest = dataList.get(position);

        holder.name.setText(serviceRequest.getUser().getName());
        holder.type.setText(serviceRequest.getServiceType().toString());
        holder.subject.setText(String.format(
                holder.itemView.getContext().getString(R.string.title_subject),
                serviceRequest.getTitle()));
        holder.date.setText(DateUtils.format(serviceRequest.getTimestamp()));

        if (!TextUtils.isEmpty(serviceRequest.getUser().getImage())) {
            Glide.with(holder.itemView)
                    .load(serviceRequest.getUser().getImage())
                    .into(holder.avatar);
        }

    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(position).getId();
    }

    public void addData(ServiceRequest serviceRequest) {
        if (serviceRequest == null) return;

        this.dataList.add(0, serviceRequest);
        notifyItemInserted(0);
    }

    public void update(ServiceRequest serviceRequest) {
        if (serviceRequest == null) return;

        int changedIndex = 0;
        boolean hasItem = false;
        for (int i = 0; i < dataList.size(); i++) {
            ServiceRequest serviceRequest1 = dataList.get(i);
            if (serviceRequest1.getId() == serviceRequest.getId()) {
                serviceRequest1.setId(serviceRequest.getId());
                serviceRequest1.setTitle(serviceRequest.getTitle());
                serviceRequest1.setUser(serviceRequest.getUser());
                serviceRequest1.setDescription(serviceRequest.getDescription());
                serviceRequest1.setServiceType(serviceRequest.getServiceType());
                changedIndex = i;
                hasItem = true;
                break;
            }
        }
        if (hasItem) {
            notifyItemChanged(changedIndex);
        } else {
            addData(serviceRequest);
        }
    }

    public void removeData(ServiceRequest serviceRequest) {
        if (serviceRequest == null) return;

        boolean isRemoved = false;
        int index = 0;
        for (int i = 0; i < dataList.size(); i++) {
            ServiceRequest serviceRequest1 = dataList.get(i);
            if (serviceRequest1.getId() == serviceRequest.getId()) {
                dataList.remove(serviceRequest1);
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

        AppCompatImageView avatar;
        AppCompatTextView name;
        AppCompatTextView subject;
        Chip type;
        AppCompatTextView date;


        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            subject = itemView.findViewById(R.id.title_subject_prefix);
            type = itemView.findViewById(R.id.type);
            date = itemView.findViewById(R.id.date);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }


}
