package com.edutask.model;

import java.time.LocalDateTime;

public class QuizSession {
    private String id;
    private String taskId;
    private int score;
    private int total;
    private LocalDateTime timestamp;

    public QuizSession(String id, String taskId, int score, int total) {
        this.id = id;
        this.taskId = taskId;
        this.score = score;
        this.total = total;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getPercentage() { return (score * 100.0) / total; }
}
