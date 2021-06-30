package com.kiratcoding.asm.ModelsClass;

public class TempOrder {
    int id, feedId, feedqty, orderid, uniquenumber;
    String feed,currentDate;
    float feedprice;

    public TempOrder() {
    }

    public TempOrder(int id, int feedId, int feedqty, String feed, float feedprice) {
        this.id = id;
        this.feedId = feedId;
        this.feedqty = feedqty;
        this.feed = feed;
        this.feedprice = feedprice;
    }

    public TempOrder(int id, int feedId, int feedqty, int orderid, int uniquenumber, String feed, String currentDate, float feedprice) {
        this.id = id;
        this.feedId = feedId;
        this.feedqty = feedqty;
        this.orderid = orderid;
        this.uniquenumber = uniquenumber;
        this.feed = feed;
        this.currentDate = currentDate;
        this.feedprice = feedprice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public int getFeedqty() {
        return feedqty;
    }

    public void setFeedqty(int feedqty) {
        this.feedqty = feedqty;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public int getUniquenumber() {
        return uniquenumber;
    }

    public void setUniquenumber(int uniquenumber) {
        this.uniquenumber = uniquenumber;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public float getFeedprice() {
        return feedprice;
    }

    public void setFeedprice(float feedprice) {
        this.feedprice = feedprice;
    }
}
