//package com.edutask;
//
//import com.edutask.persistence.*;
//import com.edutask.service.*;
//import com.edutask.events.EventBus;
//import com.edutask.ui.MainFrame;
//import com.edutask.ui.SplashScreen;
//import com.edutask.util.IconGenerator;
//import javax.swing.*;
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Properties;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            String appDataDir = System.getProperty("user.home") + File.separator + ".edutask" + File.separator + "data";
//            Files.createDirectories(Paths.get(appDataDir));
//            System.setProperty("app.data.dir", appDataDir);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // Set system look and feel
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // ✅ SHOW SPLASH SCREEN FIRST
//        SplashScreen.showSplash(() -> {
//            SwingUtilities.invokeLater(() -> {
//                try {
//                    // Load configuration
//                    Properties config = loadConfig();
//                    String storeType = config.getProperty("store.type", "file");
//
//                    // Initialize store
//                    Store store;
//                    if (storeType.equals("jdbc")) {
//                        System.out.println("Using JDBC (SQLite) storage...");
//                        store = new SqliteStore();
//                    } else {
//                        System.out.println("Using File storage...");
//                        store = new FileStore();
//                    }
//
//                    // Initialize services
//                    EventBus eventBus = new EventBus();
//                    TaskService taskService = new TaskService(store, eventBus);
//                    QuizService quizService = new QuizService();
//                    SearchService searchService = new SearchService();
//
//                    // Initialize store and task service
//                    taskService.initialize();
//
//                    // ✅ CREATE MAIN FRAME ONCE
//                    MainFrame mainFrame = new MainFrame(taskService, quizService,
//                            searchService, eventBus);
//
//                    // ✅ SET APP ICON
//                    IconGenerator.setAppIcon(mainFrame);
//
//                    // ✅ SHOW FRAME
//                    mainFrame.setVisible(true);
//
//                    System.out.println("EduTask Manager started successfully!");
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(null,
//                            "Fatal error starting application:\n" + e.getMessage(),
//                            "Startup Error",
//                            JOptionPane.ERROR_MESSAGE);
//                    System.exit(1);
//                }
//            });
//        });
//    }
//
//    private static Properties loadConfig() {
//        Properties props = new Properties();
//        File configFile = new File("resources/app.properties");
//
//        if (configFile.exists()) {
//            try (FileInputStream fis = new FileInputStream(configFile)) {
//                props.load(fis);
//            } catch (IOException e) {
//                System.err.println("Error loading config: " + e.getMessage());
//            }
//        } else {
//            // Create default config
//            props.setProperty("store.type", "file");
//            props.setProperty("theme", "light");
//
//            try {
//                configFile.getParentFile().mkdirs();
//                try (FileOutputStream fos = new FileOutputStream(configFile)) {
//                    props.store(fos, "EduTask Manager Configuration");
//                }
//            } catch (IOException e) {
//                System.err.println("Error saving default config: " + e.getMessage());
//            }
//        }
//
//        return props;
//    }
//}
package com.edutask;

import com.edutask.persistence.*;
import com.edutask.service.*;
import com.edutask.events.EventBus;
import com.edutask.ui.MainFrame;
import com.edutask.ui.SplashScreen;
import com.edutask.util.IconGenerator;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // ✅ INITIALIZE ALL REQUIRED DIRECTORIES
        initializeApplicationDirectories();

        // ✅ SET LOOK AND FEEL
        setLookAndFeel();

        // ✅ SHOW SPLASH SCREEN AND START APP
        SplashScreen.showSplash(() -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    startApplication();
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

    /**
     * Initialize all required directories for the application
     * Creates:
     * - ~/.edutask/data         (task data storage)
     * - ~/.edutask/backups      (backup files)
     * - ~/.edutask/resources    (icons, templates, etc.)
     * - ~/.edutask/exports      (PDF exports, CSV imports)
     * - ~/.edutask/logs         (error logs)
     */
    private static void initializeApplicationDirectories() {
        try {
            String userHome = System.getProperty("user.home");
            String appHome = userHome + File.separator + ".edutask";

            // Create all required directories
            String[] directories = {
                    appHome,                                           // Main app directory
                    appHome + File.separator + "data",                // Task data
                    appHome + File.separator + "backups",             // Backups
                    appHome + File.separator + "resources",           // Resources
                    appHome + File.separator + "exports",             // PDF/CSV exports
                    appHome + File.separator + "logs"                 // Application logs
            };

            for (String dir : directories) {
                Files.createDirectories(Paths.get(dir));
                System.out.println("[OK] Directory ready: " + dir);
            }

            // Set system properties for easy access throughout app
            System.setProperty("app.home", appHome);
            System.setProperty("app.data.dir", appHome + File.separator + "data");
            System.setProperty("app.backup.dir", appHome + File.separator + "backups");
            System.setProperty("app.export.dir", appHome + File.separator + "exports");
            System.setProperty("app.log.dir", appHome + File.separator + "logs");

            System.out.println("[OK] All application directories initialized");

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to create application directories!");
            System.err.println(e.getMessage());
            e.printStackTrace();
            // Don't exit - app might still work with permissions issues
        }
    }

    /**
     * Set the system look and feel to match the OS
     */
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("[OK] Look and feel set successfully");
        } catch (Exception e) {
            System.err.println("[WARNING] Could not set system look and feel");
            e.printStackTrace();
            // Continue anyway - default look and feel will be used
        }
    }

    /**
     * Start the main application
     */
    private static void startApplication() throws Exception {
        System.out.println("[STARTING] EduTask Manager initialization...");

        // Load configuration
        Properties config = loadConfiguration();
        String storeType = config.getProperty("store.type", "file");

        System.out.println("[CONFIG] Storage type: " + storeType);

        // Initialize persistence layer
        Store store;
        if (storeType.equals("jdbc")) {
            System.out.println("[INIT] Using JDBC (SQLite) storage...");
            store = new SqliteStore();
        } else {
            System.out.println("[INIT] Using File storage...");
            store = new FileStore();
        }

        // Initialize service layer
        EventBus eventBus = new EventBus();
        TaskService taskService = new TaskService(store, eventBus);
        QuizService quizService = new QuizService();
        SearchService searchService = new SearchService();

        // Initialize persistence
        taskService.initialize();
        System.out.println("[OK] Task service initialized");

        // Create and display main frame
        MainFrame mainFrame = new MainFrame(taskService, quizService, searchService, eventBus);
        IconGenerator.setAppIcon(mainFrame);
        mainFrame.setVisible(true);

        System.out.println("[SUCCESS] EduTask Manager started successfully!");
    }

    /**
     * Load or create application configuration
     */
    private static Properties loadConfiguration() {
        Properties props = new Properties();

        String appHome = System.getProperty("user.home") + File.separator + ".edutask";
        String configPath = appHome + File.separator + "app.properties";
        File configFile = new File(configPath);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                System.out.println("[OK] Configuration loaded from: " + configPath);
            } catch (IOException e) {
                System.err.println("[WARNING] Error loading config: " + e.getMessage());
                createDefaultConfiguration(props);
            }
        } else {
            System.out.println("[INFO] No existing configuration found, creating default...");
            createDefaultConfiguration(props);
            saveConfiguration(props, configFile);
        }

        return props;
    }

    /**
     * Create default configuration properties
     */
    private static void createDefaultConfiguration(Properties props) {
        props.setProperty("store.type", "file");
        props.setProperty("theme", "light");
        props.setProperty("app.version", "1.0.0");
        props.setProperty("app.language", "en");
        System.out.println("[OK] Default configuration created");
    }

    /**
     * Save configuration to file
     */
    private static void saveConfiguration(Properties props, File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "EduTask Manager Configuration - Do not edit manually");
                System.out.println("[OK] Configuration saved to: " + configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
