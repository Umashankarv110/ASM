package com.kiratcoding.asm.ModelsClass;

public class MonthName {
    private int position;
    private String mName;

    public MonthName(int position, String mName) {
        this.position = position;
        this.mName = mName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
