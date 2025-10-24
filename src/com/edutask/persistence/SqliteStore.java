package com.edutask.persistence;

import com.edutask.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SqliteStore implements Store {
    private static final String DB_URL = "jdbc:sqlite:data/edutask.db";
    private Connection conn;

    @Override
    public void initialize() throws Exception {
        conn = DriverManager.getConnection(DB_URL);
        createTables();
    }

    private void createTables() throws SQLException {
        String taskTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                details TEXT,
                category TEXT NOT NULL,
                subject TEXT,
                topic TEXT,
                tag TEXT,
                due_date TEXT,
                priority INTEGER,
                status TEXT,
                created TEXT,
                modified TEXT
            )
        """;

        String sessionTable = """
            CREATE TABLE IF NOT EXISTS quiz_sessions (
                id TEXT PRIMARY KEY,
                task_id TEXT,
                score INTEGER,
                total INTEGER,
                timestamp TEXT
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(taskTable);
            stmt.execute(sessionTable);
        }
    }

    @Override
    public void saveTasks(List<Task> tasks) throws Exception {
        conn.createStatement().execute("DELETE FROM tasks");

        String sql = """
            INSERT INTO tasks (id, title, details, category, subject, topic, tag,
                             due_date, priority, status, created, modified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Task task : tasks) {
                pstmt.setString(1, task.getId());
                pstmt.setString(2, task.getTitle());
                pstmt.setString(3, task.getDetails());
                pstmt.setString(4, task.getCategory().name());

                if (task instanceof StudyTask st) {
                    pstmt.setString(5, st.getSubject());
                    pstmt.setString(6, st.getTopic());
                    pstmt.setString(7, null);
                } else if (task instanceof PersonalTask pt) {
                    pstmt.setString(5, null);
                    pstmt.setString(6, null);
                    pstmt.setString(7, pt.getTag());
                }

                pstmt.setString(8, task.getDueDate().toString());
                pstmt.setInt(9, task.getPriority());
                pstmt.setString(10, task.getStatus().name());
                pstmt.setString(11, task.getCreated().toString());
                pstmt.setString(12, task.getModified().toString());
                pstmt.executeUpdate();
            }
        }
    }

    @Override
    public List<Task> loadTasks() throws Exception {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = Category.valueOf(rs.getString("category"));
                Task task;

                if (category == Category.STUDY) {
                    task = new StudyTask(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("details"),
                            LocalDate.parse(rs.getString("due_date")),
                            rs.getInt("priority"),
                            rs.getString("subject"),
                            rs.getString("topic")
                    );
                } else {
                    task = new PersonalTask(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("details"),
                            LocalDate.parse(rs.getString("due_date")),
                            rs.getInt("priority"),
                            rs.getString("tag")
                    );
                }

                task.setStatus(Status.valueOf(rs.getString("status")));
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public void saveQuizSession(QuizSession session) throws Exception {
        String sql = """
            INSERT INTO quiz_sessions (id, task_id, score, total, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, session.getId());
            pstmt.setString(2, session.getTaskId());
            pstmt.setInt(3, session.getScore());
            pstmt.setInt(4, session.getTotal());
            pstmt.setString(5, session.getTimestamp().toString());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<QuizSession> loadQuizSessions() throws Exception {
        List<QuizSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM quiz_sessions";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sessions.add(new QuizSession(
                        rs.getString("id"),
                        rs.getString("task_id"),
                        rs.getInt("score"),
                        rs.getInt("total")
                ));
            }
        }
        return sessions;
    }

    @Override
    public void close() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
