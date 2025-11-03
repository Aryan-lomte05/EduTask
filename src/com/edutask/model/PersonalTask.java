package com.edutask.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class PersonalTask extends Task {
    private String tag;

    // Original constructor (time defaults to 12:00)
    public PersonalTask(String id, String title, String details, LocalDate dueDate,
                        int priority, String tag) {
        super(id, title, details, Category.PERSONAL, dueDate, priority);
        this.tag = tag;
    }

    // NEW: Constructor with dueTime
    public PersonalTask(String id, String title, String details, LocalDate dueDate,
                        LocalTime dueTime, int priority, String tag) {
        this(id, title, details, dueDate, priority, tag);
        this.dueTime = dueTime;
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
