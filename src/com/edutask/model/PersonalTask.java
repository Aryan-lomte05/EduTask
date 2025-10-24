package com.edutask.model;

import java.time.LocalDate;

public class PersonalTask extends Task {
    private String tag; // Health, Work, Hobby, etc.

    public PersonalTask(String id, String title, String details, LocalDate dueDate,
                        int priority, String tag) {
        super(id, title, details, Category.PERSONAL, dueDate, priority);
        this.tag = tag;
    }

    public String getTag() { return tag; }
    public void setTag(String tag) {
        this.tag = tag;
        this.modified = java.time.LocalDateTime.now();
    }

    @Override
    public String getDisplaySubject() { return tag; }

    @Override
    public String getDisplayTopic() { return "—"; }
}
