package com.rendox.routinetracker;

import androidx.annotation.NonNull;

public class Routine {

    private String name;
    private int imageResourceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public Routine(String name) {
        this.name = name;
    }

    public Routine(String name, int imageResourceId) {
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    public static final Routine[] routines = {
            new Routine("English",R.drawable.studying),
            new Routine("Workout"),
            new Routine("Programming")
    };

    @NonNull
    public String toString(){
        return this.name;
    }
}
