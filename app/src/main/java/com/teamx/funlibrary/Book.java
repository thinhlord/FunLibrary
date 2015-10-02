package com.teamx.funlibrary;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by ruler_000 on 26/09/2015.
 * Project: Fun Library
 */
public class Book implements Serializable{
    //book_name, book_image, book_description, book_author, book_publisher, book_year, book_id
    public int id;
    public String name;
    public String imageUrl;
    public String description;
    public String author;
    public String publisher;
    public int year;

    public Book(JSONObject object) {
        id = object.optInt("id");
        name = object.optString("book_name");
        imageUrl = object.optString("book_image");
        if (imageUrl != null && !imageUrl.contains("http")) imageUrl = "http://128.199.167.255/soa/" + imageUrl;
        description = object.optString("book_description");
        author = object.optString("book_author");
        publisher = object.optString("book_publisher");
        year = object.optInt("book_year");
    }

    public Book() {
        id = -1;
        name = "";
        imageUrl = "";
        description = "";
        author = "";
        publisher = "";
        year = 0;
    }
}
