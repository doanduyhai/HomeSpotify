package fr.ippon.home_spotify.entity;

import java.io.Serializable;

public class PerformerByStyle implements Serializable{

    private String performer;
    private String style;

    public PerformerByStyle(String performer, String style) {
        this.performer = performer;
        this.style = style;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}