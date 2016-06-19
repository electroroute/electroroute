package com.tfg.evelyn.electroroute_v10;

/**
 * Created by Evelyn on 17/05/2016.
 */
public class CommentsAsListObject {



    private String user;
    private String date;
    private String comment;


    private String title;
    private float rating;

    public CommentsAsListObject(String usr,String date, String comment,float rating,String title){
        super();
        this.user=usr;
        this.comment=comment;
        this.date=date;
        this.rating=rating;
        this.title=title;


    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }


}
