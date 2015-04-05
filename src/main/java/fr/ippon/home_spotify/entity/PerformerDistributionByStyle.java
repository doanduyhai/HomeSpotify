package fr.ippon.home_spotify.entity;

import java.io.Serializable;

public class PerformerDistributionByStyle implements Serializable {

    private String type;
    private String style;
    private int count;

    public PerformerDistributionByStyle(String type, String style, int count) {
        this.type = type;
        this.style = style;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}