package com.edutask.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class StudyTask extends Task {
    private String subject;
    private String topic;

    // Original constructor (time defaults to 12:00)
    public StudyTask(String id, String title, String details, LocalDate dueDate,
                     int priority, String subject, String topic) {
        super(id, title, details, Category.STUDY, dueDate, priority);
        this.subject = subject;
        this.topic = topic;
    }

    // NEW: Constructor with dueTime
    public StudyTask(String id, String title, String details, LocalDate dueDate,
                     LocalTime dueTime, int priority, String subject, String topic) {
        this(id, title, details, dueDate, priority, subject, topic);
        this.dueTime = dueTime;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) {
        this.subject = subject;
        this.modified = java.time.LocalDateTime.now();
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) {
        this.topic = topic;
        this.modified = java.time.LocalDateTime.now();
    }

    @Override
    public String getDisplaySubject() { return subject; }

    @Override
    public String getDisplayTopic() { return topic; }
}
