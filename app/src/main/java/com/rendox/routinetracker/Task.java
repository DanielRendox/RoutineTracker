package com.rendox.routinetracker;

import androidx.annotation.NonNull;

public class Task {

    private String name;
    private String description;
    private String link;

    public static final Task[] english = {
            new Task("2 BBC LE videos",
                    "Watch 2 BBC LE videos from the playlist",
                    "https://www.youtube.com/playlist?list=PLepsjGhKogZ8nP8jH3b-jekPPfDsGM4ab"),
            new Task("3 grammar and 4 vocabulary exercises on the site",
                             null,"https://www.englishrevealed.co.uk"),
            new Task("write new things",
                    "write down new material from the video",null),
            new Task("Anki cards",null,null)
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Task(String name, String description, String link) {
        this.name = name;
        this.description = description;
        this.link = link;
    }

    @NonNull
    public String toString(){
        return this.name;
    }
}
