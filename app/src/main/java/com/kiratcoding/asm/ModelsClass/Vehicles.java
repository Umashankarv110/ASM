package com.kiratcoding.asm.ModelsClass;

public class Vehicles {

    int id, uniquenumber;
    String employeeName, type, name, date, time;
    float reimbursement;

    public Vehicles() {
    }

    public Vehicles(int id, String type, float reimbursement) {
        this.id = id;
        this.type = type;
        this.reimbursement = reimbursement;
    }

    public Vehicles(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public Vehicles(int id, int uniquenumber, String employeeName, String type, String name, String date, String time, float reimbursement) {
        this.id = id;
        this.uniquenumber = uniquenumber;
        this.employeeName = employeeName;
        this.type = type;
        this.name = name;
        this.date = date;
        this.time = time;
        this.reimbursement = reimbursement;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getReimbursement() {
        return reimbursement;
    }

    public void setReimbursement(float reimbursement) {
        this.reimbursement = reimbursement;
    }

    @Override
    public String toString() {
        return this.getType(); // What to display in the Spinner list.
    }
}
