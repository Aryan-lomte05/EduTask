package com.edutask.persistence;

import com.edutask.model.Task;
import com.edutask.model.QuizSession;
import java.util.List;

public interface Store {
    void initialize() throws Exception;
    void saveTasks(List<Task> tasks) throws Exception;
    List<Task> loadTasks() throws Exception;
    void saveQuizSession(QuizSession session) throws Exception;
    List<QuizSession> loadQuizSessions() throws Exception;
    void close() throws Exception;
}
