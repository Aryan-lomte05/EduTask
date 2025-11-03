package com.edutask.service;

import com.edutask.model.*;
import com.edutask.persistence.Store;
import com.edutask.events.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TaskService {
    private Store store;
    private List<Task> tasks;
    private EventBus eventBus;

    public TaskService(Store store, EventBus eventBus) {
        this.store = store;
        this.eventBus = eventBus;
        this.tasks = new ArrayList<>();
    }

    public void initialize() throws Exception {
        store.initialize();
        tasks = store.loadTasks();
    }

    public void addTask(Task task) throws Exception {
        tasks.add(task);
        store.saveTasks(tasks);
    }

    public void updateTask(Task task) throws Exception {
        Task existing = findById(task.getId());
        if (existing != null) {
            tasks.remove(existing);
            tasks.add(task);
            store.saveTasks(tasks);
        }
    }

    public void deleteTask(String id) throws Exception {
        tasks.removeIf(t -> t.getId().equals(id));
        store.saveTasks(tasks);
    }

    public void completeTask(String id) throws Exception {
        Task task = findById(id);
        if (task != null) {
            task.setStatus(Status.COMPLETED);
            store.saveTasks(tasks);
            eventBus.publish(new TaskCompletedEvent(task));
        }
    }

    public Task findById(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<Task> getTasksByStatus(Status status) {
        return tasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }
    // In TaskService.java, add:
    public List<Task> getTasksByDate(LocalDate date) {
        return tasks.stream()
                .filter(t -> t.getDueDate().equals(date))
                .collect(java.util.stream.Collectors.toList());
    }


    public List<Task> getTodayTasks() {
        return tasks.stream()
                .filter(t -> t.getDueDate().equals(java.time.LocalDate.now()))
                .collect(Collectors.toList());
    }

    public int getStreak() {
        // Simple streak: count quiz sessions (simplified for demo)
        try {
            return (int) store.loadQuizSessions().stream().count();
        } catch (Exception e) {
            System.err.println("Error loading streak: " + e.getMessage());
            return 0;  // Return 0 if error
        }
    }

    public void close() throws Exception {
        store.close();
    }
}
