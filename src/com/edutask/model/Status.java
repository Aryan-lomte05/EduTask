package com.edutask.model;

public enum Status {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String display;

    Status(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
