package com.kiratcoding.asm.ModelsClass;

public class Leaves {
    int id,uniquenumber,status;
    String reason, emergencyContact, fromDate, toDate, timestamp;
    int dayCount;

    public Leaves(){}

    public Leaves(int id, String reason, String fromDate, String toDate, String timestamp, int status, int dayCount) {
        this.id = id;
        this.reason = reason;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.timestamp = timestamp;
        this.status = status;
        this.dayCount = dayCount;
    }

    public Leaves(int id, int uniquenumber, String reason, String emergencyContact, String fromDate, String toDate, String timestamp) {
        this.id = id;
        this.uniquenumber = uniquenumber;
        this.reason = reason;
        this.emergencyContact = emergencyContact;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUniquenumber() {
        return uniquenumber;
    }

    public void setUniquenumber(int uniquenumber) {
        this.uniquenumber = uniquenumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public int getStatus() {
        return status;
    }

    public void setLeaveStatus(int status) {
        this.status = status;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
