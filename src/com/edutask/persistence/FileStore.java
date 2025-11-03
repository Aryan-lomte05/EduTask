package com.edutask.persistence;

import com.edutask.model.*;
import com.edutask.util.AppException;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileStore implements Store {
    private static final String DATA_DIR = "data/";
    private static final String BACKUP_DIR = "backups/";
    private static final String TASKS_FILE = DATA_DIR + "tasks.json";
    private static final String SESSIONS_FILE = DATA_DIR + "sessions.json";

    private Gson gson;

    public FileStore() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())  // ✅ NEW
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void initialize() throws Exception {
        Files.createDirectories(Paths.get(DATA_DIR));
        Files.createDirectories(Paths.get(BACKUP_DIR));
        Files.createDirectories(Paths.get(DATA_DIR + "notes/"));
    }

    @Override
    public void saveTasks(List<Task> tasks) throws Exception {
        String json = gson.toJson(tasks);
        Files.writeString(Paths.get(TASKS_FILE), json);
        createBackup();
    }

    @Override
    public List<Task> loadTasks() throws Exception {
        if (!Files.exists(Paths.get(TASKS_FILE))) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(TASKS_FILE));
        Task[] taskArray = gson.fromJson(json, Task[].class);
        return taskArray == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(taskArray));
    }

    @Override
    public void saveQuizSession(QuizSession session) throws Exception {
        List<QuizSession> sessions = loadQuizSessions();
        sessions.add(session);
        String json = gson.toJson(sessions);
        Files.writeString(Paths.get(SESSIONS_FILE), json);
    }

    @Override
    public List<QuizSession> loadQuizSessions() throws Exception {
        if (!Files.exists(Paths.get(SESSIONS_FILE))) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(SESSIONS_FILE));
        QuizSession[] array = gson.fromJson(json, QuizSession[].class);
        return array == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array));
    }

    private void createBackup() {
        try {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String backupFile = BACKUP_DIR + date + "_tasks.json";
            Files.copy(Paths.get(TASKS_FILE), Paths.get(backupFile),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Backup failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // No-op for file store
    }

    // Adapters
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value.toString());
        }
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString());
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value.toString());
        }
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString());
        }
    }

    // ✅ NEW: LocalTime adapter
    private static class LocalTimeAdapter extends TypeAdapter<LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(value != null ? value.toString() : "12:00");
        }
        @Override
        public LocalTime read(JsonReader in) throws IOException {
            String timeStr = in.nextString();
            return timeStr != null && !timeStr.isEmpty() ? LocalTime.parse(timeStr) : LocalTime.of(12, 0);
        }
    }

    private static class TaskAdapter implements JsonDeserializer<Task>, JsonSerializer<Task> {
        @Override
        public Task deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Category category = Category.valueOf(obj.get("category").getAsString());

            if (category == Category.STUDY) {
                return context.deserialize(json, StudyTask.class);
            } else {
                return context.deserialize(json, PersonalTask.class);
            }
        }

        @Override
        public JsonElement serialize(Task src, java.lang.reflect.Type typeOfSrc,
                                     JsonSerializationContext context) {
            return context.serialize(src, src.getClass());
        }
    }
}
