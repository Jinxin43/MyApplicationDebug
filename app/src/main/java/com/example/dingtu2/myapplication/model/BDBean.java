package com.example.dingtu2.myapplication.model;

public class BDBean {
    private String latitude;
    private String longitude;
    private String height;
    private int gpstime;
    private String x;
    private String y;
    private String srid;
    private String bdcode;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public int getGpstime() {
        return gpstime;
    }

    public void setGpstime(int gpstime) {
        this.gpstime = gpstime;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public String getBdcode() {
        return bdcode;
    }

    public void setBdcode(String bdcode) {
        this.bdcode = bdcode;
    }
}
