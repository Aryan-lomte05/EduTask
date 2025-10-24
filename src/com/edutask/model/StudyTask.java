package com.edutask.model;

import java.time.LocalDate;

public class StudyTask extends Task {
    private String subject;
    private String topic;

    public StudyTask(String id, String title, String details, LocalDate dueDate,
                     int priority, String subject, String topic) {
        super(id, title, details, Category.STUDY, dueDate, priority);
        this.subject = subject;
        this.topic = topic;
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
