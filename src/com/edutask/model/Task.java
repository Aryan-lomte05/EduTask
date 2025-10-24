package com.edutask.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Task {
    protected String id;
    protected String title;
    protected String details;
    protected Category category;
    protected LocalDate dueDate;
    protected int priority; // 1-5
    protected Status status;
    protected LocalDateTime created;
    protected LocalDateTime modified;

    public Task(String id, String title, String details, Category category,
                LocalDate dueDate, int priority) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.category = category;
        this.dueDate = dueDate;
        this.priority = Math.min(5, Math.max(1, priority));
        this.status = Status.TODO;
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.modified = LocalDateTime.now();
    }
    public String getDetails() { return details; }
    public void setDetails(String details) {
        this.details = details;
        this.modified = LocalDateTime.now();
    }
    public Category getCategory() { return category; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.modified = LocalDateTime.now();
    }
    public int getPriority() { return priority; }
    public void setPriority(int priority) {
        this.priority = Math.min(5, Math.max(1, priority));
        this.modified = LocalDateTime.now();
    }
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.modified = LocalDateTime.now();
    }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }

    public abstract String getDisplaySubject();
    public abstract String getDisplayTopic();
}
