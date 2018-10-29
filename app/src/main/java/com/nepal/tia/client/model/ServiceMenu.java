package com.nepal.tia.client.model;

import com.nepal.tia.model.ServiceType;

import java.io.Serializable;

public class ServiceMenu implements Serializable {

    private int id;
    private String title;
    private ServiceType type;

    public ServiceMenu(int id, String title, ServiceType type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }
}
