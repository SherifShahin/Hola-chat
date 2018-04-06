package com.downloader.hola;

/**
 * Created by Lenovo on 3/10/2018.
 */

public class single_user
{
    public String name;
    public String status;
    public String image;
    public String thumb_image;

    public single_user()
    {

    }

    public single_user (String name,String status,String image,String thumb_image)
    {
        this.name=name;
        this.status=status;
        this.image=image;
        this.thumb_image=thumb_image;
    }


    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {return thumb_image;}

    public void setThumb_image(String thumb_image) {this.thumb_image = thumb_image;}
}

