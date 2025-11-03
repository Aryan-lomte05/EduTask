package com.edutask.ui;

import com.edutask.model.Task;
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
    public TaskFormPanel taskFormPanel;  // Make public or add getter
    public TaskListPanel taskListPanel;

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

    public void editTask(Task task) {
        taskFormPanel.loadTask(task);
        // Optionally switch to task form tab if you have tabs
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

        taskListPanel = new TaskListPanel(this, taskService);
        taskFormPanel = new TaskFormPanel(this, taskService);
        statusBar = new StatusBar(taskService);
        analyticsPanel = new AnalyticsPanel(taskService, eventBus);

        // NEW: Create compact left panel with filters + actions
        JPanel leftPanel = createCompactLeftPanel();

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
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());
    }

    // NEW METHOD: Compact left panel with 50% filters + 50% actions
    private JPanel createCompactLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PremiumTheme.CORK_BACKGROUND);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, PremiumTheme.CORK_DARK));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(2);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setTopComponent(createStylishFilterPanel());
        splitPane.setBottomComponent(createStylishActionPanel());

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStylishFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(205, 170, 125));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 3, 1, 3, new Color(139, 90, 43)),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));

        // Header
        JLabel titleLabel = new JLabel("FILTERS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(80, 40, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setForeground(new Color(139, 90, 43));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));

        // Store combos for filter logic
        JComboBox<String> categoryCombo = createStylishCombo(new String[]{
                "All", "Study", "Personal", "Work", "Sports", "Health", "Movies", "Games", "Travel", "Shopping", "Social"
        });

        JComboBox<String> statusCombo = createStylishCombo(new String[]{
                "All", "To Do", "In Progress", "Completed"
        });

        JComboBox<String> priorityCombo = createStylishCombo(new String[]{
                "All", "1", "2", "3", "4", "5"
        });

        JComboBox<String> dueCombo = createStylishCombo(new String[]{
                "All", "Today", "This Week", "Overdue"
        });

        // Add filters with labels
        panel.add(createFilterRow("Category:", categoryCombo));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createFilterRow("Status:", statusCombo));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createFilterRow("Priority:", priorityCombo));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createFilterRow("Due Date:", dueCombo));
        panel.add(Box.createVerticalStrut(12));

        // Action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(200, 32));

        JButton applyBtn = createStylishButton("APPLY", new Color(70, 130, 180));
        JButton resetBtn = createStylishButton("RESET", new Color(180, 100, 70));

        applyBtn.addActionListener(e -> {
            String cat = (String) categoryCombo.getSelectedItem();
            String stat = (String) statusCombo.getSelectedItem();
            String pri = (String) priorityCombo.getSelectedItem();
            String due = (String) dueCombo.getSelectedItem();
            applyFilters(cat, stat, pri, due);
            com.edutask.audio.SoundManager.getInstance().playClick();
        });

        resetBtn.addActionListener(e -> {
            categoryCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
            priorityCombo.setSelectedIndex(0);
            dueCombo.setSelectedIndex(0);
            applyFilters("All", "All", "All", "All");
            com.edutask.audio.SoundManager.getInstance().playClick();
        });

        buttonPanel.add(applyBtn);
        buttonPanel.add(resetBtn);
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createStylishActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(195, 160, 115));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 3, 3, 3, new Color(139, 90, 43)),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 0.5;
        gbc.weighty = 0.2;

        // Header
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("QUICK ACTIONS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(new Color(80, 40, 20));
        panel.add(titleLabel, gbc);

        // Separator
        gbc.gridy = 1;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(139, 90, 43));
        panel.add(sep, gbc);

        gbc.gridwidth = 1;

        // Row 1
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(createPremiumActionButton("ADD", new Color(139, 28, 59), e -> {
            taskFormPanel.clearForm();
            com.edutask.audio.SoundManager.getInstance().playAdd();
        }), gbc);

        gbc.gridx = 1;
        panel.add(createPremiumActionButton("EDIT", new Color(70, 130, 180), e -> {
            taskListPanel.editSelectedTask();
        }), gbc);

        // Row 2
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(createPremiumActionButton("DELETE", new Color(200, 60, 60), e -> {
            taskListPanel.deleteSelectedTask();
        }), gbc);

        gbc.gridx = 1;
        panel.add(createPremiumActionButton("DONE", new Color(100, 180, 100), e -> {
            taskListPanel.completeSelectedTask();
        }), gbc);

        // Row 3
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(createPremiumActionButton("CALENDAR", new Color(180, 100, 180), e -> {
            openCalendarWindow();
            com.edutask.audio.SoundManager.getInstance().playClick();
        }), gbc);

        gbc.gridx = 1;
        panel.add(createPremiumActionButton("REFRESH", new Color(100, 150, 200), e -> {
            refreshAll();
            com.edutask.audio.SoundManager.getInstance().playClick();
        }), gbc);

        return panel;
    }

    private JPanel createFilterRow(String label, JComboBox<String> combo) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(200, 28));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 10));
        labelComp.setForeground(new Color(60, 30, 10));
        labelComp.setPreferredSize(new Dimension(65, 28));

        row.add(labelComp, BorderLayout.WEST);
        row.add(combo, BorderLayout.CENTER);

        return row;
    }

    private JComboBox<String> createStylishCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Arial", Font.PLAIN, 10));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(50, 50, 50));
        combo.setBorder(BorderFactory.createLineBorder(new Color(139, 90, 43), 1));
        combo.setMaximumSize(new Dimension(125, 26));
        return combo;
    }

    private JButton createStylishButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private JButton createPremiumActionButton(String text, Color bgColor,
                                              java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 10));
        btn.setForeground(Color.RED);
        btn.setBackground(bgColor);
        btn.setFocusPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 8, true),
                BorderFactory.createEmptyBorder(8, 3, 8, 3)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addActionListener(action);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2, true),
                        BorderFactory.createEmptyBorder(8, 3, 8, 3)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(bgColor.darker(), 2, true),
                        BorderFactory.createEmptyBorder(8, 3, 8, 3)
                ));
            }
        });

        return btn;
    }


    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(PremiumTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Arial", Font.PLAIN, 11));
        combo.setMaximumSize(new Dimension(200, 28));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // ADD THIS GETTER METHOD for TaskDetailsDialog
    public TaskFormPanel getTaskFormPanel() {
        return taskFormPanel;
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
        JMenuItem exportPDF = new JMenuItem("Export to PDF...");
        exportPDF.addActionListener(e -> {
            com.edutask.util.PDFExporter.showExportDialog(taskService, this);
        });
        fileMenu.add(exportPDF);
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
