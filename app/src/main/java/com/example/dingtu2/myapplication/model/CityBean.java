package com.example.dingtu2.myapplication.model;

public class CityBean {
    private String Type;
    private Float mapSize;
    private String CityName;
    private String status;


    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public Float getMapSize() {
        return mapSize;
    }

    public void setMapSize(Float mapSize) {
        this.mapSize = mapSize;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
