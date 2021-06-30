package com.kiratcoding.asm.ModelsClass;

public class OfflineTracking {
    private String uniquenumber, employeeName, type, latitude, longitude, date, time;
    private int status;

    public OfflineTracking() {
    }

    public OfflineTracking(String uniquenumber, String employeeName, String type, String latitude, String longitude, String date, String time, int status) {
        this.uniquenumber = uniquenumber;
        this.employeeName = employeeName;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getUniquenumber() {
        return uniquenumber;
    }

    public void setUniquenumber(String uniquenumber) {
        this.uniquenumber = uniquenumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
