package com.nepal.tia.client.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.nepal.tia.R;
import com.nepal.tia.model.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class ClientServiceAdapter extends RecyclerView.Adapter<ClientServiceAdapter.ViewHolder> {

    private final List<ServiceInfo> dataList = new ArrayList<>();

    public ClientServiceAdapter() {
        super();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_client, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ServiceInfo serviceInfo = dataList.get(position);

        holder.title.setText(serviceInfo.getTitle());
        if (TextUtils.isEmpty(serviceInfo.getAddress())) {
            holder.address.setVisibility(View.GONE);
        } else
            holder.address.setText(serviceInfo.getAddress());
        if (TextUtils.isEmpty(serviceInfo.getEmail())) {
            holder.email.setVisibility(View.GONE);
        } else
            holder.email.setText(serviceInfo.getEmail());
        if (TextUtils.isEmpty(serviceInfo.getContact())) {
            holder.contact.setVisibility(View.GONE);
        } else
            holder.contact.setText(serviceInfo.getContact());
        if (TextUtils.isEmpty(serviceInfo.getWebsite())) {
            holder.website.setVisibility(View.GONE);
        } else
            holder.website.setText(serviceInfo.getWebsite());

        if (!TextUtils.isEmpty(serviceInfo.getImage())) {
            Glide.with(holder.itemView)
                    .load(serviceInfo.getImage())
                    .into(holder.image);
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

    public void setItem(List<ServiceInfo> items) {
        this.dataList.clear();
        this.dataList.addAll(items);
        notifyDataSetChanged();
    }

    public void addData(ServiceInfo serviceInfo) {
        if (serviceInfo == null) return;

        this.dataList.add(0, serviceInfo);
        notifyItemInserted(0);
    }

    public void update(ServiceInfo serviceInfo) {
        if (serviceInfo == null) return;

        int changedIndex = 0;
        boolean hasItem = false;
        for (int i = 0; i < dataList.size(); i++) {
            ServiceInfo serviceInfo1 = dataList.get(i);
            if (serviceInfo1.getId() == serviceInfo.getId()) {
                serviceInfo1.setId(serviceInfo.getId());
                serviceInfo1.setAddress(serviceInfo.getAddress());
                serviceInfo1.setContact(serviceInfo.getContact());
                serviceInfo1.setEmail(serviceInfo.getEmail());
                serviceInfo1.setTitle(serviceInfo.getTitle());
                serviceInfo1.setWebsite(serviceInfo.getWebsite());
                serviceInfo1.setImage(serviceInfo.getImage());
                serviceInfo1.setType(serviceInfo.getType());
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
        AppCompatTextView address;
        AppCompatTextView contact;
        AppCompatTextView email;
        AppCompatTextView website;
        AppCompatImageView image;


        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            address = itemView.findViewById(R.id.address);
            contact = itemView.findViewById(R.id.contact);
            email = itemView.findViewById(R.id.email);
            website = itemView.findViewById(R.id.website);
            image = itemView.findViewById(R.id.image);
        }
    }


}
