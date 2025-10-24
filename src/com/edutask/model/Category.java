package com.edutask.model;

public enum Category {
    STUDY("Study"),
    PERSONAL("Personal");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
