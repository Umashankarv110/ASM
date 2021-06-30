package com.kiratcoding.asm.ModelsClass;

public class Employee {

    int id, uniquenumber, isTrash;
    String email, pass, name, phone, dob, gender, address, designation, date, time;

    public Employee(int uniquenumber, String email, String name, String gender) {
        this.uniquenumber = uniquenumber;
        this.email = email;
        this.name = name;
        this.gender = gender;
    }

    public Employee(int id, int uniquenumber, int isTrash, String email, String pass, String name, String phone, String dob, String gender, String address, String designation, String date, String time) {
        this.id = id;
        this.uniquenumber = uniquenumber;
        this.isTrash = isTrash;
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.designation = designation;
        this.date = date;
        this.time = time;
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

    public int getIsTrash() {
        return isTrash;
    }

    public void setIsTrash(int isTrash) {
        this.isTrash = isTrash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
