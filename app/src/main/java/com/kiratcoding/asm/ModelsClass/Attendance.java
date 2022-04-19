package com.kiratcoding.asm.ModelsClass;

public class Attendance {
    int id, uniquenumber, vehicleId;
    float startReading,closeReading;
    String status,employeeName,currentDate,fromLocation,toLocation,note,startLatitude,startLongitude,startImage,startTime;
    String closeLatitude,closeLongitude,closeImage,closeTime,checkOutNote,timestamp;
    private int upload_status;
    String vehicleName,amount,distance;

    String day,dayname, month, year;

    public Attendance() {
    }

    public Attendance(String status, String note , String day, String dayname, String month, String year) {
        this.status = status;
        this.note = note;
        this.day = day;
        this.dayname = dayname;
        this.month = month;
        this.year = year;
    }

    public Attendance(int id, String vehicleName, float startReading, float closeReading, String fromLocation, String toLocation, String status, String note, String startTime, String closeTime, String distance, String amount) {
        this.id = id;
        this.vehicleName = vehicleName;
        this.startReading = startReading;
        this.closeReading = closeReading;
        this.status = status;
        this.note = note;
        this.startTime = startTime;
        this.closeTime = closeTime;
        this.distance = distance;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.amount = amount;
    }

    public Attendance(int uniquenumber, int vehicleId, String status) {
        this.uniquenumber = uniquenumber;
        this.vehicleId = vehicleId;
        this.status = status;
    }

    public Attendance(String status,int vehicleId,int uniquenumber, String employeeName, String currentDate, String startLatitude, String startLongitude, float startReading, String startImage, String startTime, String note, String fromLocation, String toLocation, int upload_status) {

        this.uniquenumber = uniquenumber;
        this.vehicleId = vehicleId;
        this.startReading = startReading;
        this.status = status;
        this.employeeName = employeeName;
        this.currentDate = currentDate;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.note = note;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.startImage = startImage;
        this.startTime = startTime;
        this.upload_status = upload_status;


    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDayname() {
        return dayname;
    }

    public void setDayname(String dayname) {
        this.dayname = dayname;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getUniquenumber() {
        return uniquenumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUniquenumber(int uniquenumber) {
        this.uniquenumber = uniquenumber;
    }

    public float getStartReading() {
        return startReading;
    }

    public void setStartReading(float startReading) {
        this.startReading = startReading;
    }

    public float getCloseReading() {
        return closeReading;
    }

    public void setCloseReading(float closeReading) {
        this.closeReading = closeReading;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(String startLatitude) {
        this.startLatitude = startLatitude;
    }

    public String getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(String startLongitude) {
        this.startLongitude = startLongitude;
    }

    public String getStartImage() {
        return startImage;
    }

    public void setStartImage(String startImage) {
        this.startImage = startImage;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getCloseLatitude() {
        return closeLatitude;
    }

    public void setCloseLatitude(String closeLatitude) {
        this.closeLatitude = closeLatitude;
    }

    public String getCloseLongitude() {
        return closeLongitude;
    }

    public void setCloseLongitude(String closeLongitude) {
        this.closeLongitude = closeLongitude;
    }

    public String getCloseImage() {
        return closeImage;
    }

    public void setCloseImage(String closeImage) {
        this.closeImage = closeImage;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getCheckOutNote() {
        return checkOutNote;
    }

    public void setCheckOutNote(String checkOutNote) {
        this.checkOutNote = checkOutNote;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
