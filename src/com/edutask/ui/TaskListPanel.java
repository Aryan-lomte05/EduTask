//package com.edutask.ui;
//
//import com.edutask.model.*;
//import com.edutask.service.*;
//import javax.swing.*;
//import javax.swing.table.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.List;
//
//public class TaskListPanel extends JPanel {
//    private MainFrame mainFrame;
//    private TaskService taskService;
//    private SearchService searchService;
//    private JTable taskTable;
//    private TaskTableModel tableModel;
//
//    public TaskListPanel(MainFrame mainFrame, TaskService taskService) {
//        this.mainFrame = mainFrame;
//        this.taskService = taskService;
//        this.searchService = new SearchService();
//
//        setLayout(new BorderLayout());
//        initializeTable();
//        addToolbar();
//    }
//
//    private void initializeTable() {
//        tableModel = new TaskTableModel(taskService.getAllTasks());
//        taskTable = new JTable(tableModel);
//        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        taskTable.setRowHeight(35);
//        taskTable.setFont(new Font("Arial", Font.PLAIN, 13));
//        taskTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
//
//        // Custom cell renderer
//        taskTable.setDefaultRenderer(Object.class, new TaskCellRenderer());
//
//        // Column widths
//        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
//        taskTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
//        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Category
//        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Subject
//        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Topic
//        taskTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Due
//        taskTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Priority
//        taskTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
//
//        // Selection listener
//        taskTable.getSelectionModel().addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                int row = taskTable.getSelectedRow();
//                if (row >= 0) {
//                    Task task = tableModel.getTaskAt(row);
//                    // Load into form (you can implement this)
//                }
//            }
//        });
//
//        // Double-click to edit
//        taskTable.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    int row = taskTable.getSelectedRow();
//                    if (row >= 0) {
//                        Task task = tableModel.getTaskAt(row);
//                        // Open edit dialog or load into form
//                    }
//                }
//            }
//        });
//
//        JScrollPane scrollPane = new JScrollPane(taskTable);
//        add(scrollPane, BorderLayout.CENTER);
//    }
//
//    private void addToolbar() {
//        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
//
//        JButton addBtn = new JButton("➕ Add");
//        addBtn.addActionListener(e -> mainFrame.taskFormPanel.clearForm());
//
//        JButton editBtn = new JButton("✏️ Edit");
//        editBtn.addActionListener(e -> editSelectedTask());
//
//        JButton deleteBtn = new JButton("🗑️ Delete");
//        deleteBtn.addActionListener(e -> deleteSelectedTask());
//
//        JButton completeBtn = new JButton("✅ Complete");
//        completeBtn.addActionListener(e -> completeSelectedTask());
//
//        JButton refreshBtn = new JButton("🔄 Refresh");
//        refreshBtn.addActionListener(e -> refreshTasks());
//
//        toolbar.add(addBtn);
//        toolbar.add(editBtn);
//        toolbar.add(deleteBtn);
//        toolbar.add(completeBtn);
//        toolbar.add(refreshBtn);
//
//        add(toolbar, BorderLayout.NORTH);
//    }
//
//    public void refreshTasks() {
//        tableModel.setTasks(taskService.getAllTasks());
//    }
//
//    public void filterTasks(String keyword, String category, String status,
//                            String priority, String dueFilter) {
//        List<Task> tasks = taskService.getAllTasks();
//
//        // Apply search
//        tasks = searchService.search(tasks, keyword);
//
//        // Apply filters
//        Category cat = category != null && !category.equals("All") ?
//                Category.valueOf(category.toUpperCase()) : null;
//        Status stat = status != null && !status.equals("All") ?
//                Status.valueOf(status.toUpperCase().replace(" ", "_")) : null;
//        Integer minPri = priority != null && !priority.equals("All") ?
//                Integer.parseInt(priority) : null;
//
//        tasks = searchService.filter(tasks, cat, stat, minPri, dueFilter);
//        tableModel.setTasks(tasks);
//    }
//
//    private void editSelectedTask() {
//        int row = taskTable.getSelectedRow();
//        if (row >= 0) {
//            Task task = tableModel.getTaskAt(row);
//            mainFrame.taskFormPanel.loadTask(task);
//        } else {
//            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
//        }
//    }
//
//    public void deleteSelectedTask() {
//        int row = taskTable.getSelectedRow();
//        if (row >= 0) {
//            Task task = tableModel.getTaskAt(row);
//            int confirm = JOptionPane.showConfirmDialog(
//                    this,
//                    "Delete task: " + task.getTitle() + "?",
//                    "Confirm Delete",
//                    JOptionPane.YES_NO_OPTION
//            );
//
//            if (confirm == JOptionPane.YES_OPTION) {
//                try {
//                    taskService.deleteTask(task.getId());
//                    refreshTasks();
//                    mainFrame.refreshAll();
//                    JOptionPane.showMessageDialog(this, "Task deleted successfully!");
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
//                            "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        }
//    }
//
//    private void completeSelectedTask() {
//        int row = taskTable.getSelectedRow();
//        if (row >= 0) {
//            Task task = tableModel.getTaskAt(row);
//            try {
//                taskService.completeTask(task.getId());
//                refreshTasks();
//                mainFrame.refreshAll();
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        } else {
//            JOptionPane.showMessageDialog(this, "Please select a task to complete.");
//        }
//    }
//
//    // Inner class: Table Model
//    private static class TaskTableModel extends AbstractTableModel {
//        private final String[] columnNames = {
//                "ID", "Title", "Category", "Subject/Tag", "Topic", "Due Date", "Priority", "Status"
//        };
//        private List<Task> tasks;
//
//        public TaskTableModel(List<Task> tasks) {
//            this.tasks = tasks;
//        }
//
//        public void setTasks(List<Task> tasks) {
//            this.tasks = tasks;
//            fireTableDataChanged();
//        }
//
//        public Task getTaskAt(int row) {
//            return tasks.get(row);
//        }
//
//        @Override
//        public int getRowCount() {
//            return tasks.size();
//        }
//
//        @Override
//        public int getColumnCount() {
//            return columnNames.length;
//        }
//
//        @Override
//        public String getColumnName(int column) {
//            return columnNames[column];
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            Task task = tasks.get(rowIndex);
//            return switch (columnIndex) {
//                case 0 -> task.getId().substring(0, Math.min(8, task.getId().length()));
//                case 1 -> task.getTitle();
//                case 2 -> task.getCategory().getDisplay();
//                case 3 -> task.getDisplaySubject();
//                case 4 -> task.getDisplayTopic();
//                case 5 -> com.edutask.util.DateUtils.getDueLabel(task.getDueDate());
//                case 6 -> "★".repeat(task.getPriority());
//                case 7 -> task.getStatus().getDisplay();
//                default -> "";
//            };
//        }
//    }
//
//    // Inner class: Cell Renderer
//    private static class TaskCellRenderer extends DefaultTableCellRenderer {
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value,
//                                                       boolean isSelected, boolean hasFocus,
//                                                       int row, int column) {
//            Component c = super.getTableCellRendererComponent(table, value, isSelected,
//                    hasFocus, row, column);
//
//            // Color coding by status
//            TaskTableModel model = (TaskTableModel) table.getModel();
//            Task task = model.getTaskAt(row);
//
//            if (!isSelected) {
//                switch (task.getStatus()) {
//                    case COMPLETED -> c.setBackground(new Color(200, 255, 200));
//                    case IN_PROGRESS -> c.setBackground(new Color(255, 255, 200));
//                    default -> c.setBackground(Color.WHITE);
//                }
//
//                // Overdue highlight
//                if (task.getDueDate().isBefore(java.time.LocalDate.now()) &&
//                        task.getStatus() != Status.COMPLETED) {
//                    c.setBackground(new Color(255, 200, 200));
//                }
//            }
//
//            return c;
//        }
//    }
//}
package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.*;
import com.edutask.ui.components.StickyNotePanel;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TaskListPanel extends JPanel {
    private MainFrame mainFrame;
    private TaskService taskService;
    private SearchService searchService;

    private JPanel notesContainer;
    private List<StickyNotePanel> noteComponents;
    private StickyNotePanel selectedNote;

    public TaskListPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;
        this.searchService = new SearchService();
        this.noteComponents = new ArrayList<>();

        setLayout(new BorderLayout());
        setOpaque(false);
        initializeUI();
        refreshTasks();
    }

    private void initializeUI() {
        // Toolbar at top
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Cork board container for sticky notes
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

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton addBtn = new JButton("➕ Add Task");
        PremiumTheme.styleButton(addBtn);
        addBtn.addActionListener(e -> mainFrame.taskFormPanel.clearForm());

        JButton editBtn = new JButton("✏️ Edit");
        PremiumTheme.styleButton(editBtn);
        editBtn.addActionListener(e -> editSelectedTask());

        JButton deleteBtn = new JButton("🗑️ Delete");
        PremiumTheme.styleButton(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSelectedTask());

        JButton completeBtn = new JButton("✅ Complete");
        PremiumTheme.styleButton(completeBtn);
        completeBtn.addActionListener(e -> completeSelectedTask());

        JButton refreshBtn = new JButton("🔄 Refresh");
        PremiumTheme.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshTasks());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(completeBtn);
        toolbar.add(refreshBtn);

        return toolbar;
    }

    public void refreshTasks() {
        displayTasks(taskService.getAllTasks());
    }

    private void displayTasks(List<Task> tasks) {
        // Clear existing notes
        notesContainer.removeAll();
        noteComponents.clear();
        selectedNote = null;

        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("📌 No tasks yet! Click 'Add Task' to create one.");
            emptyLabel.setFont(PremiumTheme.FONT_TITLE);
            emptyLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
            notesContainer.add(emptyLabel);
        } else {
            for (Task task : tasks) {
                StickyNotePanel notePanel = new StickyNotePanel(task);

                // Click to select
                notePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        selectNote(notePanel);
                    }
                });

                noteComponents.add(notePanel);
                notesContainer.add(notePanel);
            }
        }

        notesContainer.revalidate();
        notesContainer.repaint();
    }

    private void selectNote(StickyNotePanel note) {
        // Deselect previous
        if (selectedNote != null) {
            selectedNote.setSelected(false);
        }

        // Select new
        selectedNote = note;
        selectedNote.setSelected(true);

        // Load into form
        mainFrame.taskFormPanel.loadTask(note.getTask());
    }

    public void filterTasks(String keyword, String category, String status,
                            String priority, String dueFilter) {
        List<Task> tasks = taskService.getAllTasks();

        // Apply search
        tasks = searchService.search(tasks, keyword);

        // Apply filters
        Category cat = category != null && !category.equals("All") ?
                Category.valueOf(category.toUpperCase()) : null;
        Status stat = status != null && !status.equals("All") ?
                Status.valueOf(status.toUpperCase().replace(" ", "_")) : null;
        Integer minPri = priority != null && !priority.equals("All") ?
                Integer.parseInt(priority) : null;

        tasks = searchService.filter(tasks, cat, stat, minPri, dueFilter);
        displayTasks(tasks);
    }

    private void editSelectedTask() {
        if (selectedNote == null) {
            JOptionPane.showMessageDialog(this,
                    "📌 Please select a sticky note to edit!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        mainFrame.taskFormPanel.loadTask(selectedNote.getTask());
    }

    public void deleteSelectedTask() {
        if (selectedNote == null) {
            JOptionPane.showMessageDialog(this,
                    "📌 Please select a sticky note to delete!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Task task = selectedNote.getTask();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete task: \"" + task.getTitle() + "\"?\n\nThis will remove the sticky note from your board.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskService.deleteTask(task.getId());
                selectedNote = null;
                refreshTasks();
                mainFrame.refreshAll();

                // Success message
                JOptionPane.showMessageDialog(this,
                        "✅ Task deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void completeSelectedTask() {
        if (selectedNote == null) {
            JOptionPane.showMessageDialog(this,
                    "📌 Please select a sticky note to complete!",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Task task = selectedNote.getTask();
        try {
            taskService.completeTask(task.getId());
            selectedNote = null;
            refreshTasks();
            mainFrame.refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom layout for wrapping sticky notes
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
