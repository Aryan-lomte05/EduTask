package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TaskListPanel extends JPanel {
    private MainFrame mainFrame;
    private TaskService taskService;
    private SearchService searchService;
    private JTable taskTable;
    private TaskTableModel tableModel;

    public TaskListPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;
        this.searchService = new SearchService();

        setLayout(new BorderLayout());
        initializeTable();
        addToolbar();
    }

    private void initializeTable() {
        tableModel = new TaskTableModel(taskService.getAllTasks());
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(35);
        taskTable.setFont(new Font("Arial", Font.PLAIN, 13));
        taskTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Custom cell renderer
        taskTable.setDefaultRenderer(Object.class, new TaskCellRenderer());

        // Column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Category
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Subject
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Topic
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Due
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Priority
        taskTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status

        // Selection listener
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = taskTable.getSelectedRow();
                if (row >= 0) {
                    Task task = tableModel.getTaskAt(row);
                    // Load into form (you can implement this)
                }
            }
        });

        // Double-click to edit
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = taskTable.getSelectedRow();
                    if (row >= 0) {
                        Task task = tableModel.getTaskAt(row);
                        // Open edit dialog or load into form
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton addBtn = new JButton("➕ Add");
        addBtn.addActionListener(e -> mainFrame.taskFormPanel.clearForm());

        JButton editBtn = new JButton("✏️ Edit");
        editBtn.addActionListener(e -> editSelectedTask());

        JButton deleteBtn = new JButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteSelectedTask());

        JButton completeBtn = new JButton("✅ Complete");
        completeBtn.addActionListener(e -> completeSelectedTask());

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshTasks());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(completeBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    public void refreshTasks() {
        tableModel.setTasks(taskService.getAllTasks());
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
        tableModel.setTasks(tasks);
    }

    private void editSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            Task task = tableModel.getTaskAt(row);
            mainFrame.taskFormPanel.loadTask(task);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
        }
    }

    public void deleteSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            Task task = tableModel.getTaskAt(row);
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete task: " + task.getTitle() + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    taskService.deleteTask(task.getId());
                    refreshTasks();
                    mainFrame.refreshAll();
                    JOptionPane.showMessageDialog(this, "Task deleted successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void completeSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            Task task = tableModel.getTaskAt(row);
            try {
                taskService.completeTask(task.getId());
                refreshTasks();
                mainFrame.refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to complete.");
        }
    }

    // Inner class: Table Model
    private static class TaskTableModel extends AbstractTableModel {
        private final String[] columnNames = {
                "ID", "Title", "Category", "Subject/Tag", "Topic", "Due Date", "Priority", "Status"
        };
        private List<Task> tasks;

        public TaskTableModel(List<Task> tasks) {
            this.tasks = tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
            fireTableDataChanged();
        }

        public Task getTaskAt(int row) {
            return tasks.get(row);
        }

        @Override
        public int getRowCount() {
            return tasks.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task task = tasks.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> task.getId().substring(0, Math.min(8, task.getId().length()));
                case 1 -> task.getTitle();
                case 2 -> task.getCategory().getDisplay();
                case 3 -> task.getDisplaySubject();
                case 4 -> task.getDisplayTopic();
                case 5 -> com.edutask.util.DateUtils.getDueLabel(task.getDueDate());
                case 6 -> "★".repeat(task.getPriority());
                case 7 -> task.getStatus().getDisplay();
                default -> "";
            };
        }
    }

    // Inner class: Cell Renderer
    private static class TaskCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            // Color coding by status
            TaskTableModel model = (TaskTableModel) table.getModel();
            Task task = model.getTaskAt(row);

            if (!isSelected) {
                switch (task.getStatus()) {
                    case COMPLETED -> c.setBackground(new Color(200, 255, 200));
                    case IN_PROGRESS -> c.setBackground(new Color(255, 255, 200));
                    default -> c.setBackground(Color.WHITE);
                }

                // Overdue highlight
                if (task.getDueDate().isBefore(java.time.LocalDate.now()) &&
                        task.getStatus() != Status.COMPLETED) {
                    c.setBackground(new Color(255, 200, 200));
                }
            }

            return c;
        }
    }
}
