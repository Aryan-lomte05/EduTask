package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.*;
import com.edutask.ui.components.StickyNotePanel;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

public class TaskListPanel extends JPanel {
    private MainFrame mainFrame;
    private TaskService taskService;
    private SearchService searchService;

    private JPanel notesContainer;
    private List<StickyNotePanel> noteComponents;
    private StickyNotePanel selectedNote;
    private Task selectedTask;  // ← ADDED: For tracking selected task

    private LocalDate currentDisplayDate;
    private JLabel dateLabel;

    public TaskListPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;
        this.searchService = new SearchService();
        this.noteComponents = new ArrayList<>();
        this.currentDisplayDate = LocalDate.now();

        setLayout(new BorderLayout());
        setOpaque(false);
        initializeUI();
        refreshTasks();
    }

    private void initializeUI() {
        // Top panel with date selector and toolbar
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);

        JPanel datePanel = createDateNavigator();
        topPanel.add(datePanel, BorderLayout.NORTH);

        JPanel toolbar = createToolbar();
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Cork board container
        notesContainer = new JPanel();
        notesContainer.setLayout(new WrapLayout(FlowLayout.LEFT, 15, 15));
        notesContainer.setOpaque(false);
        notesContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(notesContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDateNavigator() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JButton prevBtn = new JButton("<");
        PremiumTheme.styleButton(prevBtn);
        prevBtn.setPreferredSize(new Dimension(50, 35));
        prevBtn.addActionListener(e -> changeDay(-1));

        JButton nextBtn = new JButton(">");
        PremiumTheme.styleButton(nextBtn);
        nextBtn.setPreferredSize(new Dimension(50, 35));
        nextBtn.addActionListener(e -> changeDay(1));

        JButton todayBtn = new JButton("Today");
        PremiumTheme.styleButton(todayBtn);
        todayBtn.addActionListener(e -> goToToday());

        dateLabel = new JLabel(formatDate(currentDisplayDate), SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(dateLabel, BorderLayout.CENTER);
        centerPanel.add(todayBtn, BorderLayout.EAST);

        panel.add(prevBtn, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(nextBtn, BorderLayout.EAST);

        return panel;
    }

    private String formatDate(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return "TODAY - " + date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        }
        return date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
    }

    private void changeDay(int delta) {
        currentDisplayDate = currentDisplayDate.plusDays(delta);
        dateLabel.setText(formatDate(currentDisplayDate));
        refreshTasks();
    }

    private void goToToday() {
        currentDisplayDate = LocalDate.now();
        dateLabel.setText(formatDate(currentDisplayDate));
        refreshTasks();
    }

    public void setDisplayDate(LocalDate date) {
        this.currentDisplayDate = date;
        if (dateLabel != null) {
            dateLabel.setText(formatDate(date));
        }
        refreshTasks();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton addBtn = new JButton("+ Add Task");
        PremiumTheme.styleButton(addBtn);
        addBtn.addActionListener(e -> {
            mainFrame.taskFormPanel.clearForm();
            com.edutask.audio.SoundManager.getInstance().playClick();
        });

        JButton editBtn = new JButton("Edit");
        PremiumTheme.styleButton(editBtn);
        editBtn.addActionListener(e -> editSelectedTask());

        JButton deleteBtn = new JButton("Delete");
        PremiumTheme.styleButton(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSelectedTask());

        JButton completeBtn = new JButton("Complete");
        PremiumTheme.styleButton(completeBtn);
        completeBtn.addActionListener(e -> completeSelectedTask());

        JButton calendarBtn = new JButton("Calendar");
        PremiumTheme.styleButton(calendarBtn);
        calendarBtn.addActionListener(e -> {
            mainFrame.openCalendarWindow();
            com.edutask.audio.SoundManager.getInstance().playClick();
        });

        JButton refreshBtn = new JButton("Refresh");
        PremiumTheme.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            refreshTasks();
            com.edutask.audio.SoundManager.getInstance().playClick();
        });

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(completeBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(calendarBtn);
        toolbar.add(refreshBtn);

        return toolbar;
    }

    public void refreshTasks() {
        List<Task> tasksForDate = taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(currentDisplayDate))
                .toList();

        displayTasks(tasksForDate);
    }

    private void displayTasks(List<Task> tasks) {
        notesContainer.removeAll();
        noteComponents.clear();
        selectedNote = null;
        selectedTask = null;

        if (tasks.isEmpty()) {
            String message = currentDisplayDate.equals(LocalDate.now()) ?
                    "📌 No tasks for today! Click 'Add Task' to create one." :
                    "📌 No tasks for this date. Use arrows to change date.";

            JLabel emptyLabel = new JLabel(message);
            emptyLabel.setFont(PremiumTheme.FONT_TITLE);
            emptyLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
            notesContainer.add(emptyLabel);
        } else {
            for (Task task : tasks) {
                // FIXED: Pass all required parameters
                StickyNotePanel notePanel = new StickyNotePanel(task, taskService, mainFrame);

                noteComponents.add(notePanel);
                notesContainer.add(notePanel);
            }
        }

        notesContainer.revalidate();
        notesContainer.repaint();
    }

    // ADDED: Method to set selected task from outside (e.g., from StickyNotePanel)
    public void setSelectedTask(Task task) {
        this.selectedTask = task;
        this.selectedNote = null;

        // Update visual selection
        for (StickyNotePanel note : noteComponents) {
            boolean isSelected = note.getTask().equals(task);
            note.setSelected(isSelected);
            if (isSelected) {
                this.selectedNote = note;
            }
        }
    }

    public void filterTasks(String keyword, String category, String status,
                            String priority, String dueFilter) {
        List<Task> tasks = taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(currentDisplayDate))
                .toList();

        tasks = searchService.search(tasks, keyword);

        Category cat = category != null && !category.equals("All") ?
                Category.valueOf(category.toUpperCase()) : null;
        Status stat = status != null && !status.equals("All") ?
                Status.valueOf(status.toUpperCase().replace(" ", "_")) : null;
        Integer minPri = priority != null && !priority.equals("All") ?
                Integer.parseInt(priority) : null;

        tasks = searchService.filter(tasks, cat, stat, minPri, dueFilter);
        displayTasks(tasks);
    }

    public void editSelectedTask() {
        if (selectedTask != null) {
            mainFrame.getTaskFormPanel().loadTask(selectedTask);
            com.edutask.audio.SoundManager.getInstance().playClick();
        } else if (selectedNote != null) {
            mainFrame.getTaskFormPanel().loadTask(selectedNote.getTask());
            com.edutask.audio.SoundManager.getInstance().playClick();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a sticky note to edit!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteSelectedTask() {
        Task taskToDelete = selectedTask != null ? selectedTask :
                (selectedNote != null ? selectedNote.getTask() : null);

        if (taskToDelete == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a sticky note to delete!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete task: \"" + taskToDelete.getTitle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskService.deleteTask(taskToDelete.getId());
                selectedNote = null;
                selectedTask = null;
                refreshTasks();
                mainFrame.refreshAll();
                com.edutask.audio.SoundManager.getInstance().playDelete();
                JOptionPane.showMessageDialog(this, "Task deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void completeSelectedTask() {
        Task taskToComplete = selectedTask != null ? selectedTask :
                (selectedNote != null ? selectedNote.getTask() : null);

        if (taskToComplete == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a sticky note to complete!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            taskService.completeTask(taskToComplete.getId());
            selectedNote = null;
            selectedTask = null;
            refreshTasks();
            mainFrame.refreshAll();
            com.edutask.audio.SoundManager.getInstance().playSuccess();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // WrapLayout class for wrapping sticky notes
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);

                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }

                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }

                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                addRow(dim, rowWidth, rowHeight);

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                if (scrollPane != null && target.isValid()) {
                    dim.width -= (hgap + 1);
                }

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }
}
