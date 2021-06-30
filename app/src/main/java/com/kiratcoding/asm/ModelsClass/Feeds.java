package com.kiratcoding.asm.ModelsClass;

public class Feeds {
    int id,isTrash;
    String name,timestamp;
    float buyprice, sellprice;

    public Feeds() {
    }

    public Feeds(int id, String name, float sellprice) {
        this.id = id;
        this.name = name;
        this.sellprice = sellprice;
    }

    public Feeds(int id, int isTrash, String name, String timestamp, float buyprice, float sellprice) {
        this.id = id;
        this.isTrash = isTrash;
        this.name = name;
        this.timestamp = timestamp;
        this.buyprice = buyprice;
        this.sellprice = sellprice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsTrash() {
        return isTrash;
    }

    public void setIsTrash(int isTrash) {
        this.isTrash = isTrash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getBuyprice() {
        return buyprice;
    }

    public void setBuyprice(float buyprice) {
        this.buyprice = buyprice;
    }

    public float getSellprice() {
        return sellprice;
    }

    public void setSellprice(float sellprice) {
        this.sellprice = sellprice;
    }
}
