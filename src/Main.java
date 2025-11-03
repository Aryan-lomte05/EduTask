package com.edutask;

import com.edutask.persistence.*;
import com.edutask.service.*;
import com.edutask.events.EventBus;
import com.edutask.ui.MainFrame;
import com.edutask.ui.SplashScreen;
import com.edutask.util.IconGenerator;
import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ SHOW SPLASH SCREEN FIRST
        SplashScreen.showSplash(() -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Load configuration
                    Properties config = loadConfig();
                    String storeType = config.getProperty("store.type", "file");

                    // Initialize store
                    Store store;
                    if (storeType.equals("jdbc")) {
                        System.out.println("Using JDBC (SQLite) storage...");
                        store = new SqliteStore();
                    } else {
                        System.out.println("Using File storage...");
                        store = new FileStore();
                    }

                    // Initialize services
                    EventBus eventBus = new EventBus();
                    TaskService taskService = new TaskService(store, eventBus);
                    QuizService quizService = new QuizService();
                    SearchService searchService = new SearchService();

                    // Initialize store and task service
                    taskService.initialize();

                    // ✅ CREATE MAIN FRAME ONCE
                    MainFrame mainFrame = new MainFrame(taskService, quizService,
                            searchService, eventBus);

                    // ✅ SET APP ICON
                    IconGenerator.setAppIcon(mainFrame);

                    // ✅ SHOW FRAME
                    mainFrame.setVisible(true);

                    System.out.println("EduTask Manager started successfully!");

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Fatal error starting application:\n" + e.getMessage(),
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
        });
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        File configFile = new File("resources/app.properties");

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        } else {
            // Create default config
            props.setProperty("store.type", "file");
            props.setProperty("theme", "light");

            try {
                configFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(configFile)) {
                    props.store(fos, "EduTask Manager Configuration");
                }
            } catch (IOException e) {
                System.err.println("Error saving default config: " + e.getMessage());
            }
        }

        return props;
    }
}
