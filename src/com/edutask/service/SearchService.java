package com.edutask.service;

import com.edutask.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchService {

    public List<Task> search(List<Task> tasks, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(tasks);
        }

        String lower = keyword.toLowerCase();
        return tasks.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(lower) ||
                        t.getDetails().toLowerCase().contains(lower) ||
                        t.getDisplaySubject().toLowerCase().contains(lower) ||
                        t.getDisplayTopic().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Task> filter(List<Task> tasks, Category category, Status status,
                             Integer minPriority, String dueFilter) {
        return tasks.stream()
                .filter(t -> category == null || t.getCategory() == category)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> minPriority == null || t.getPriority() >= minPriority)
                .filter(t -> matchesDueFilter(t, dueFilter))
                .collect(Collectors.toList());
    }

    private boolean matchesDueFilter(Task task, String filter) {
        if (filter == null || filter.equals("All")) return true;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate due = task.getDueDate();

        return switch (filter) {
            case "Today" -> due.equals(today);
            case "This Week" -> due.isBefore(today.plusDays(7)) && !due.isBefore(today);
            case "Overdue" -> due.isBefore(today) && task.getStatus() != Status.COMPLETED;
            default -> true;
        };
    }
}
