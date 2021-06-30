package com.kiratcoding.asm.ModelsClass;

public class Order {
    int id, partyId, uniquenumber;
    float amountPaid;
    String partyName, employeeName, paymentType,orderStatus,date,time;

    public Order() {
    }

    public Order(int id, String partyName, String orderStatus, String date) {
        this.id = id;
        this.partyName = partyName;
        this.orderStatus = orderStatus;
        this.date = date;
    }

    public Order(int id, int partyId, int uniquenumber, float amountPaid, String partyName, String employeeName, String paymentType, String orderStatus, String date, String time) {
        this.id = id;
        this.partyId = partyId;
        this.uniquenumber = uniquenumber;
        this.amountPaid = amountPaid;
        this.partyName = partyName;
        this.employeeName = employeeName;
        this.paymentType = paymentType;
        this.orderStatus = orderStatus;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public int getUniquenumber() {
        return uniquenumber;
    }

    public void setUniquenumber(int uniquenumber) {
        this.uniquenumber = uniquenumber;
    }

    public float getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(float amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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
}
