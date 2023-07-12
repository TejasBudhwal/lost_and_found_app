package com.example.lostandfound;

public class User {

    public String fullName;
    public String phoneNo;

    public User() {

    }

    public User(String fullName, String phoneNo) {
        this.fullName = fullName;
        this.phoneNo = phoneNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}