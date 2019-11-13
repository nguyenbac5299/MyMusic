package com.bkav.mymusic;


import android.database.Cursor;

public class Song {
    private int id;

    private  String name;

    private  String file;

    private String singer;

    private int duration;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getFile() {
        return file;
    }

    public String getSinger() {
        return singer;
    }

    public int getDuration() {
        return duration;
    }

    public Song(Cursor cursor) {
    }

    public Song(int id, String title, String file, String artist, int duration  ) {
        this.name =title;
        this.id=id;
        this.file=file;
        this.singer = artist;
        this.duration= duration;
    }
}


