package com.nepal.tia.model;

import java.io.Serializable;

public enum ServiceType implements Serializable {

    HOTEL,
    HOSPITAL,
    DESTINATION,
    RESCUE,
    POLICE_STATION,
    GUIDE;

    public static ServiceType fromIndex(int serviceIndex) {
        ServiceType type = null;
        switch (serviceIndex) {
            case 0:
                type = HOTEL;
                break;
            case 1:
                type = HOSPITAL;
                break;
            case 2:
                type = DESTINATION;
                break;
            case 3:
                type = RESCUE;
                break;
            case 4:
                type = POLICE_STATION;
                break;
            case 5:
                type = GUIDE;
                break;
        }
        return type;
    }

    public static int toIndex(ServiceType type) {
        int index = 0;
        switch (type) {
            case HOTEL:
                index = 0;
                break;
            case HOSPITAL:
                index = 1;
                break;
            case DESTINATION:
                index = 2;
                break;
            case RESCUE:
                index = 3;
                break;
            case POLICE_STATION:
                index = 4;
                break;
            case GUIDE:
                index = 5;
                break;
        }
        return index;
    }
}
