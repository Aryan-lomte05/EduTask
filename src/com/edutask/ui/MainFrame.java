//package com.edutask.ui;
//
//import com.edutask.service.*;
//import com.edutask.events.*;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//
//public class MainFrame extends JFrame {
//    private TaskService taskService;
//    private QuizService quizService;
//    private SearchService searchService;
//    private ReactionService reactionService;
//    private EventBus eventBus;
//
//    private TaskListPanel taskListPanel;
//    public TaskFormPanel taskFormPanel;
//    private FilterPanel filterPanel;
//    private StatusBar statusBar;
//    private AnalyticsPanel analyticsPanel;
//
//    public MainFrame(TaskService taskService, QuizService quizService,
//                     SearchService searchService, EventBus eventBus) {
//        this.taskService = taskService;
//        this.quizService = quizService;
//        this.searchService = searchService;
//        this.eventBus = eventBus;
//
//        // Initialize reaction service
//        this.reactionService = new ReactionService(quizService);
//        eventBus.subscribe(TaskCompletedEvent.class, reactionService);
//
//        initializeUI();
//        setupWindowListener();
//        registerKeyboardShortcuts();
//    }
//
//    private void initializeUI() {
//        setTitle("EduTask Manager - TaskForge Edition");
//        setSize(1400, 800);
//        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        setLocationRelativeTo(null);
//
//        // Set Look and Feel
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // Create panels
//        filterPanel = new FilterPanel(this);
//        taskListPanel = new TaskListPanel(this, taskService);
//        taskFormPanel = new TaskFormPanel(this, taskService);
//        statusBar = new StatusBar(taskService);
//        analyticsPanel = new AnalyticsPanel(taskService, eventBus);
//
//        // Layout
//        setLayout(new BorderLayout(10, 10));
//
//        // Left: Filters
//        JScrollPane filterScroll = new JScrollPane(filterPanel);
//        filterScroll.setPreferredSize(new Dimension(220, 0));
//        filterScroll.setBorder(BorderFactory.createTitledBorder("Filters"));
//        add(filterScroll, BorderLayout.WEST);
//
//        // Center: Task List
//        JPanel centerPanel = new JPanel(new BorderLayout());
//        centerPanel.add(createSearchBar(), BorderLayout.NORTH);
//        centerPanel.add(taskListPanel, BorderLayout.CENTER);
//        add(centerPanel, BorderLayout.CENTER);
//
//        // Right: Form + Analytics
//        JPanel rightPanel = new JPanel(new BorderLayout());
//        JTabbedPane rightTabs = new JTabbedPane();
//        rightTabs.addTab("Task Form", taskFormPanel);
//        rightTabs.addTab("Analytics", analyticsPanel);
//        rightPanel.add(rightTabs, BorderLayout.CENTER);
//        rightPanel.setPreferredSize(new Dimension(400, 0));
//        add(rightPanel, BorderLayout.EAST);
//
//        // Bottom: Status Bar
//        add(statusBar, BorderLayout.SOUTH);
//
//        // Menu Bar
//        setJMenuBar(createMenuBar());
//    }
//
//    private JPanel createSearchBar() {
//        JPanel panel = new JPanel(new BorderLayout(5, 5));
//        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//        JTextField searchField = new JTextField();
//        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
//
//        JButton searchBtn = new JButton("🔍 Search");
//        searchBtn.addActionListener(e -> performSearch(searchField.getText()));
//
//        searchField.addActionListener(e -> performSearch(searchField.getText()));
//
//        panel.add(new JLabel("Quick Search:"), BorderLayout.WEST);
//        panel.add(searchField, BorderLayout.CENTER);
//        panel.add(searchBtn, BorderLayout.EAST);
//
//        return panel;
//    }
//
//    private JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
//
//        // File Menu
//        JMenu fileMenu = new JMenu("File");
//        fileMenu.setMnemonic(KeyEvent.VK_F);
//
//        JMenuItem newItem = new JMenuItem("New Task", KeyEvent.VK_N);
//        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
//        newItem.addActionListener(e -> taskFormPanel.clearForm());
//
//        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
//        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
//        saveItem.addActionListener(e -> taskFormPanel.saveTask());
//
//        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
//        exitItem.addActionListener(e -> exitApplication());
//
//        fileMenu.add(newItem);
//        fileMenu.add(saveItem);
//        fileMenu.addSeparator();
//        fileMenu.add(exitItem);
//
//        // Edit Menu
//        JMenu editMenu = new JMenu("Edit");
//        JMenuItem findItem = new JMenuItem("Find", KeyEvent.VK_F);
//        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
//        editMenu.add(findItem);
//
//        // View Menu
//        JMenu viewMenu = new JMenu("View");
//        JMenuItem refreshItem = new JMenuItem("Refresh", KeyEvent.VK_R);
//        refreshItem.addActionListener(e -> refreshAll());
//        viewMenu.add(refreshItem);
//
//        // Help Menu
//        JMenu helpMenu = new JMenu("Help");
//        JMenuItem aboutItem = new JMenuItem("About");
//        aboutItem.addActionListener(e -> showAboutDialog());
//        helpMenu.add(aboutItem);
//
//        menuBar.add(fileMenu);
//        menuBar.add(editMenu);
//        menuBar.add(viewMenu);
//        menuBar.add(helpMenu);
//
//        return menuBar;
//    }
//
//    private void registerKeyboardShortcuts() {
//        // Delete key for quick delete
//        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
//                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
//        getRootPane().getActionMap().put("deleteTask", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskListPanel.deleteSelectedTask();
//            }
//        });
//
//        // Escape to clear form
//        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
//                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearForm");
//        getRootPane().getActionMap().put("clearForm", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskFormPanel.clearForm();
//            }
//        });
//    }
//
//    private void setupWindowListener() {
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                exitApplication();
//            }
//        });
//    }
//
//    private void exitApplication() {
//        int confirm = JOptionPane.showConfirmDialog(
//                this,
//                "Save all changes and exit?",
//                "Confirm Exit",
//                JOptionPane.YES_NO_OPTION
//        );
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            try {
//                taskService.close();
//                System.exit(0);
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(this, "Error closing: " + ex.getMessage());
//            }
//        }
//    }
//
//    public void performSearch(String keyword) {
//        taskListPanel.filterTasks(keyword, null, null, null, "All");
//    }
//
//    public void applyFilters(String category, String status, String priority, String dueFilter) {
//        taskListPanel.filterTasks("", category, status, priority, dueFilter);
//    }
//
//    public void refreshAll() {
//        taskListPanel.refreshTasks();
//        statusBar.updateStatus();
//        analyticsPanel.refresh();
//    }
//
//    private void showAboutDialog() {
//        String message = """
//            EduTask Manager (TaskForge Edition)
//            Version 1.0
//
//            A comprehensive task management system with
//            intelligent quiz-based learning reinforcement.
//
//            Features:
//            • Study & Personal Task Management
//            • Context-Aware Reactions (Quiz/Journal)
//            • Search, Filter, Sort Capabilities
//            • Analytics & Streak Tracking
//            • File & Optional JDBC Persistence
//
//            Developed using:
//            Java Swing, OOP, Event Handling, Packages
//
//            © 2025 KJSSE OOPL Project
//            """;
//
//        JOptionPane.showMessageDialog(this, message, "About EduTask Manager",
//                JOptionPane.INFORMATION_MESSAGE);
//    }
//}
package com.edutask.ui;

import com.edutask.service.*;
import com.edutask.events.*;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.edutask.ui.calendar.CalendarViewPanel;
public class MainFrame extends JFrame {
    private TaskService taskService;
    private QuizService quizService;
    private SearchService searchService;
    private ReactionService reactionService;
    private EventBus eventBus;

    private TaskListPanel taskListPanel;
    public TaskFormPanel taskFormPanel;  // public for access
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
        setTitle("EduTask Manager - Premium Edition 🎨");
        setSize(1500, 850);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply premium look and feel
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

        // Left: Filters (with premium styling)
        JPanel filterContainer = new JPanel(new BorderLayout());
        filterContainer.setOpaque(false);
        JScrollPane filterScroll = new JScrollPane(filterPanel);
        filterScroll.setPreferredSize(new Dimension(240, 0));
        filterScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                "📁 Filters", 0, 0, PremiumTheme.FONT_TITLE, PremiumTheme.TEXT_PRIMARY
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
        rightTabs.addTab("📝 Task Form", taskFormPanel);
        rightTabs.addTab("📊 Analytics", analyticsPanel);
        CalendarViewPanel calendarPanel = new CalendarViewPanel(taskService);
        rightTabs.addTab("Calendar", calendarPanel);
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

        JButton searchBtn = new JButton("🔍 Search");
        PremiumTheme.styleButton(searchBtn);
        searchBtn.addActionListener(e -> performSearch(searchField.getText()));

        searchField.addActionListener(e -> performSearch(searchField.getText()));

        panel.add(new JLabel("🔍 Quick Search:", SwingConstants.LEFT), BorderLayout.WEST);
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

    private void showAboutDialog() {
        String message = """
            EduTask Manager - Premium Edition 🎨
            Version 2.0
            
            A beautiful cork board-style task manager with
            intelligent quiz-based learning reinforcement.
            
            Features:
            • Premium Cork Board UI with Sticky Notes
            • Study & Personal Task Management
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
