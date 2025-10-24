package com.edutask.model;

public enum Category {
    STUDY("Study"),
    PERSONAL("Personal"),
    WORK("Work"),
    SPORTS("Sports"),
    HEALTH("Health"),
    MOVIES("Movies"),
    GAMES("Games"),
    TRAVEL("Travel"),
    SHOPPING("Shopping"),
    SOCIAL("Social"),
    OTHER("Other");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
