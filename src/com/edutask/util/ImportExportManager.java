package com.edutask.util;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ImportExportManager {
    private TaskService taskService;
    private Gson gson;

    public ImportExportManager(TaskService taskService) {
        this.taskService = taskService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    // Export to JSON
    public void exportToJSON(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        chooser.setSelectedFile(new File("edutask_backup_" + System.currentTimeMillis() + ".json"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                gson.toJson(taskService.getAllTasks(), writer);
                JOptionPane.showMessageDialog(parent, "Exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Export failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Import from JSON
    public void importFromJSON(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (FileReader reader = new FileReader(chooser.getSelectedFile())) {
                Task[] tasks = gson.fromJson(reader, Task[].class);

                int count = 0;
                for (Task task : tasks) {
                    try {
                        taskService.addTask(task);
                        count++;
                    } catch (Exception e) {
                        // Skip duplicates
                    }
                }

                JOptionPane.showMessageDialog(parent,
                        "Imported " + count + " tasks successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Import failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Export to CSV
    public void exportToCSV(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        chooser.setSelectedFile(new File("edutask_export_" + System.currentTimeMillis() + ".csv"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(chooser.getSelectedFile())) {
                writer.println("ID,Title,Details,Category,Status,Priority,DueDate,Subject,Topic,Tag");

                for (Task task : taskService.getAllTasks()) {
                    writer.printf("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s%n",
                            task.getId(),
                            escapeCSV(task.getTitle()),
                            escapeCSV(task.getDetails()),
                            task.getCategory().getDisplay(),
                            task.getStatus().getDisplay(),
                            task.getPriority(),
                            task.getDueDate().toString(),
                            task instanceof StudyTask ? ((StudyTask) task).getSubject() : "",
                            task instanceof StudyTask ? ((StudyTask) task).getTopic() : "",
                            task instanceof PersonalTask ? ((PersonalTask) task).getTag() : ""
                    );
                }

                JOptionPane.showMessageDialog(parent, "CSV exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Export failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // LocalDate adapter for Gson
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
}
