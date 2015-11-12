package com.datastax.home_spotify.entity;

import java.io.Serializable;

public class AlbumByDecadeAndCountry implements Serializable {
    private String decade;
    private String country;
    private int albumCount;

    public AlbumByDecadeAndCountry() {
    }

    public AlbumByDecadeAndCountry(String decade, String country, int albumCount) {
        this.decade = decade;
        this.country = country;
        this.albumCount = albumCount;
    }

    public String getDecade() {
        return decade;
    }

    public void setDecade(String decade) {
        this.decade = decade;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }
}
