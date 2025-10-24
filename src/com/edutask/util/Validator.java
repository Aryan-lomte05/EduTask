package com.edutask.util;

import java.time.LocalDate;

public class Validator {

    public static void validateTaskTitle(String title) throws AppException {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException("Title cannot be empty");
        }
        if (title.length() > 100) {
            throw new AppException("Title too long (max 100 characters)");
        }
    }

    public static void validateDueDate(LocalDate date) throws AppException {
        if (date == null) {
            throw new AppException("Due date cannot be null");
        }
        if (date.isBefore(LocalDate.now().minusDays(1))) {
            throw new AppException("Due date cannot be in the past");
        }
    }

    public static void validatePriority(int priority) throws AppException {
        if (priority < 1 || priority > 5) {
            throw new AppException("Priority must be between 1 and 5");
        }
    }

    public static void validateStudyFields(String subject, String topic) throws AppException {
        if (subject == null || subject.trim().isEmpty()) {
            throw new AppException("Subject is required for Study tasks");
        }
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException("Topic is required for Study tasks");
        }
    }
}
