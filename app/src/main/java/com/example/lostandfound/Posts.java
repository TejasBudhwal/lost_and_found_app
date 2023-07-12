package com.example.lostandfound;

import android.net.Uri;

public class Posts
{
    public String publisher, location, message, fullName, phone;
    public String postImage;

    public Posts()
    {

    }

    @Override
    public String toString() {
        return "Posts{" +
                "publisher='" + publisher + '\'' +
                ", location='" + location + '\'' +
                ", message='" + message + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", postImage='" + postImage + '\'' +
                '}';
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
