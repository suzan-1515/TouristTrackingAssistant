package com.nepal.tia.admin.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.nepal.tia.R;
import com.nepal.tia.event.OnItemActionListener;
import com.nepal.tia.model.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    private final List<ServiceInfo> dataList = new ArrayList<>();
    private OnItemActionListener<ServiceInfo> listener;

    public AdminServiceAdapter(final OnItemActionListener<ServiceInfo> listener) {
        super();
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_admin, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ServiceInfo serviceInfo = dataList.get(position);

        holder.title.setText(serviceInfo.getTitle());
        holder.type.setText(serviceInfo.getType().toString());
        if (!TextUtils.isEmpty(serviceInfo.getImage())) {
            Glide.with(holder.itemView)
                    .load(serviceInfo.getImage())
                    .into(holder.image);
        }

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemEditAction(v, position, serviceInfo);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemDeleteAction(v, position, serviceInfo);
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

    public void update(ServiceInfo serviceInfo) {
        if (serviceInfo == null) return;

        int changedIndex = 0;
        boolean hasItem = false;
        for (int i = 0; i < dataList.size(); i++) {
            ServiceInfo serviceInfo1 = dataList.get(i);
            if (serviceInfo1.getId() == serviceInfo.getId()) {
                serviceInfo1.setId(serviceInfo.getId());
                serviceInfo1.setTitle(serviceInfo.getTitle());
                serviceInfo1.setType(serviceInfo.getType());
                serviceInfo1.setImage(serviceInfo.getImage());
                changedIndex = i;
                hasItem = true;
                break;
            }
        }
        if (hasItem) {
            notifyItemChanged(changedIndex);
        } else {
            addData(serviceInfo);
        }
    }

    public void addData(ServiceInfo serviceInfo) {
        if (serviceInfo == null) return;

        this.dataList.add(0, serviceInfo);
        notifyItemInserted(0);
    }

    public void removeData(ServiceInfo serviceInfo) {
        if (serviceInfo == null) return;

        boolean isRemoved = false;
        int index = 0;
        for (int i = 0; i < dataList.size(); i++) {
            ServiceInfo serviceInfo1 = dataList.get(i);
            if (serviceInfo1.getId() == serviceInfo.getId()) {
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

        AppCompatTextView title;
        AppCompatImageView image;
        Chip type;
        AppCompatButton editButton;
        AppCompatButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            type = itemView.findViewById(R.id.type);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
        }
    }
}
