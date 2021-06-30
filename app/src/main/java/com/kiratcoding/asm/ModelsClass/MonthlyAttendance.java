package com.kiratcoding.asm.ModelsClass;

public class MonthlyAttendance {
    int dayNumber;
    String attendanceStatus;

    public MonthlyAttendance(int dayNumber, String attendanceStatus) {
        this.dayNumber = dayNumber;
        this.attendanceStatus = attendanceStatus;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
