package com.edutask.ui;

import com.edutask.service.*;
import com.edutask.events.*;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;  // ← ADD THIS IMPORT

public class MainFrame extends JFrame {
    private TaskService taskService;
    private QuizService quizService;
    private SearchService searchService;
    private ReactionService reactionService;
    private EventBus eventBus;

    public TaskListPanel taskListPanel;  // Changed to public
    public TaskFormPanel taskFormPanel;
    private FilterPanel filterPanel;
    private StatusBar statusBar;
    private AnalyticsPanel analyticsPanel;

    public MainFrame(TaskService taskService, QuizService quizService,
                     SearchService searchService, EventBus eventBus) {
        this.taskService = taskService;
        this.quizService = quizService;
        this.searchService = searchService;
        this.eventBus = eventBus;

        this.reactionService = new ReactionService(quizService);
        eventBus.subscribe(TaskCompletedEvent.class, reactionService);

        initializeUI();
        setupWindowListener();
        registerKeyboardShortcuts();
    }

    private void initializeUI() {
        setTitle("EduTask Manager - Premium Edition");
        setSize(1500, 850);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main cork board panel
        JPanel mainPanel = PremiumTheme.createCorkPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create panels
        filterPanel = new FilterPanel(this);
        taskListPanel = new TaskListPanel(this, taskService);
        taskFormPanel = new TaskFormPanel(this, taskService);
        statusBar = new StatusBar(taskService);
        analyticsPanel = new AnalyticsPanel(taskService, eventBus);

        // Left: Filters
        JPanel filterContainer = new JPanel(new BorderLayout());
        filterContainer.setOpaque(false);
        JScrollPane filterScroll = new JScrollPane(filterPanel);
        filterScroll.setPreferredSize(new Dimension(240, 0));
        filterScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                "Filters", 0, 0, PremiumTheme.FONT_TITLE, PremiumTheme.TEXT_PRIMARY
        ));
        filterScroll.setOpaque(false);
        filterScroll.getViewport().setOpaque(false);
        filterContainer.add(filterScroll, BorderLayout.CENTER);

        // Center: Task Board
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(createSearchBar(), BorderLayout.NORTH);
        centerPanel.add(taskListPanel, BorderLayout.CENTER);

        // Right: Form + Analytics tabs
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.setFont(PremiumTheme.FONT_BODY);
        rightTabs.addTab("Task Form", taskFormPanel);
        rightTabs.addTab("Analytics", analyticsPanel);
        rightPanel.add(rightTabs, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(420, 0));

        // Assembly
        mainPanel.add(filterContainer, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());
    }

    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextField searchField = new JTextField();
        searchField.setFont(PremiumTheme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton searchBtn = new JButton("Search");
        PremiumTheme.styleButton(searchBtn);
        searchBtn.addActionListener(e -> performSearch(searchField.getText()));

        searchField.addActionListener(e -> performSearch(searchField.getText()));

        // NEW:
        panel.add(new JLabel("Quick Search:", SwingConstants.LEFT), BorderLayout.WEST);

        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchBtn, BorderLayout.EAST);

        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PremiumTheme.CORK_LIGHT);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(PremiumTheme.FONT_BODY);

        JMenuItem newItem = new JMenuItem("New Task", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> taskFormPanel.clearForm());

        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> taskFormPanel.saveTask());
        // In createMenuBar() method, add after saveItem:

        JMenuItem exportJSONItem = new JMenuItem("Export to JSON");
        exportJSONItem.addActionListener(e -> {
            com.edutask.util.ImportExportManager manager =
                    new com.edutask.util.ImportExportManager(taskService);
            manager.exportToJSON(this);
        });

        JMenuItem importJSONItem = new JMenuItem("Import from JSON");
        importJSONItem.addActionListener(e -> {
            com.edutask.util.ImportExportManager manager =
                    new com.edutask.util.ImportExportManager(taskService);
            manager.importFromJSON(this);
            refreshAll();
        });

        JMenuItem exportCSVItem = new JMenuItem("Export to CSV");
        exportCSVItem.addActionListener(e -> {
            com.edutask.util.ImportExportManager manager =
                    new com.edutask.util.ImportExportManager(taskService);
            manager.exportToCSV(this);
        });

        fileMenu.add(exportJSONItem);
        fileMenu.add(importJSONItem);
        fileMenu.add(exportCSVItem);
        fileMenu.addSeparator();


        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(PremiumTheme.FONT_BODY);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void registerKeyboardShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        getRootPane().getActionMap().put("deleteTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taskListPanel.deleteSelectedTask();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearForm");
        getRootPane().getActionMap().put("clearForm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taskFormPanel.clearForm();
            }
        });
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Save all changes and exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskService.close();
                System.exit(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error closing: " + ex.getMessage());
            }
        }
    }

    public void performSearch(String keyword) {
        taskListPanel.filterTasks(keyword, null, null, null, "All");
    }

    public void applyFilters(String category, String status, String priority, String dueFilter) {
        taskListPanel.filterTasks("", category, status, priority, dueFilter);
    }

    public void refreshAll() {
        taskListPanel.refreshTasks();
        statusBar.updateStatus();
        analyticsPanel.refresh();
    }

    // ← ADD THESE TWO METHODS:

    public void openCalendarWindow() {
        JDialog calendarDialog = new JDialog(this, "EduTask Calendar - Year/Month/Week Views", false);
        calendarDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create calendar panel with reference to main frame
        com.edutask.ui.calendar.CalendarViewPanel calendarPanel =
                new com.edutask.ui.calendar.CalendarViewPanel(taskService, this);

        calendarDialog.setContentPane(calendarPanel);
        calendarDialog.setSize(1300, 750);
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
    }

    public void showDayTasks(LocalDate date) {
        // Switch main window to show that day's tasks
        taskListPanel.setDisplayDate(date);

        // Bring main window to front
        toFront();
        requestFocus();
    }

    private void showAboutDialog() {
        String message = """
            EduTask Manager - Premium Edition
            Version 2.0
            
            A beautiful cork board-style task manager with
            intelligent quiz-based learning reinforcement.
            
            Features:
            • Premium Cork Board UI with Sticky Notes
            • Study & Personal Task Management
            • Calendar Views (Year/Month/Week)
            • Context-Aware Reactions (Quiz/Journal)
            • Advanced Search & Filtering
            • Analytics & Streak Tracking
            • File & Optional JDBC Persistence
            
            © 2025 KJSSE OOPL Project
            """;

        JOptionPane.showMessageDialog(this, message, "About EduTask Manager",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
