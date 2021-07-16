package com.example.testapp;


public class Post {

    private int id;
    private String desc;
    private String lat;
    private String longi;
    private String areaa;
    private byte[] photo;
    private int status;

    public Post(int id, String desc, String lat, String longi, String areaa, byte[] photo, int status) {
        this.id = id;
        this.desc = desc;
        this.lat = lat;
        this.longi = longi;
        this.areaa = areaa;
        this.photo = photo;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public String getAreaa() {
        return areaa;
    }

    public void setAreaa(String areaa) {
        this.areaa = areaa;
    }

    public byte[] getPhoto() {
        return this.photo;
    }


    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
