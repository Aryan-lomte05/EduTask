package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.util.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TaskFormPanel extends JPanel {
    private MainFrame mainFrame;
    private TaskService taskService;

    private JTextField titleField;
    private JTextArea detailsArea;
    private JComboBox<String> categoryCombo;
    private JTextField subjectField;
    private JTextField topicField;
    private JTextField tagField;
    private JTextField dueDateField;
    private JSpinner prioritySpinner;

    private Task currentTask; // For editing

    public TaskFormPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeForm();
    }

    private void initializeForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:*"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        // Details
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Details:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        detailsArea = new JTextArea(4, 20);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        formPanel.add(detailsScroll, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JLabel("Category:*"), gbc);

        gbc.gridx = 1;
        categoryCombo = new JComboBox<>(new String[]{"Study", "Personal"});
        categoryCombo.addActionListener(e -> toggleCategoryFields());
        formPanel.add(categoryCombo, gbc);

        // Subject (Study only)
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        subjectField = new JTextField(20);
        formPanel.add(subjectField, gbc);

        // Topic (Study only)
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Topic:"), gbc);

        gbc.gridx = 1;
        topicField = new JTextField(20);
        formPanel.add(topicField, gbc);

        // Tag (Personal only)
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Tag:"), gbc);

        gbc.gridx = 1;
        tagField = new JTextField(20);
        tagField.setEnabled(false);
        formPanel.add(tagField, gbc);

        // Due Date
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Due Date:*"), gbc);

        gbc.gridx = 1;
        dueDateField = new JTextField(LocalDate.now().toString());
        dueDateField.setToolTipText("Format: YYYY-MM-DD");
        formPanel.add(dueDateField, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Priority:*"), gbc);

        gbc.gridx = 1;
        prioritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        formPanel.add(prioritySpinner, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton saveBtn = new JButton("💾 Save");
        saveBtn.addActionListener(e -> saveTask());

        JButton clearBtn = new JButton("🔄 Clear");
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void toggleCategoryFields() {
        boolean isStudy = categoryCombo.getSelectedItem().equals("Study");
        subjectField.setEnabled(isStudy);
        topicField.setEnabled(isStudy);
        tagField.setEnabled(!isStudy);
    }

    public void saveTask() {
        try {
            // Validate
            String title = titleField.getText().trim();
            Validator.validateTaskTitle(title);

            LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
            Validator.validateDueDate(dueDate);

            int priority = (Integer) prioritySpinner.getValue();
            Validator.validatePriority(priority);

            String details = detailsArea.getText().trim();
            boolean isStudy = categoryCombo.getSelectedItem().equals("Study");

            Task task;
            if (isStudy) {
                String subject = subjectField.getText().trim();
                String topic = topicField.getText().trim();
                Validator.validateStudyFields(subject, topic);

                String id = currentTask != null ? currentTask.getId() : UUID.randomUUID().toString();
                task = new StudyTask(id, title, details, dueDate, priority, subject, topic);
            } else {
                String tag = tagField.getText().trim();
                if (tag.isEmpty()) tag = "General";

                String id = currentTask != null ? currentTask.getId() : UUID.randomUUID().toString();
                task = new PersonalTask(id, title, details, dueDate, priority, tag);
            }

            if (currentTask != null) {
                task.setStatus(currentTask.getStatus());
                taskService.updateTask(task);
                JOptionPane.showMessageDialog(this, "Task updated successfully!");
            } else {
                taskService.addTask(task);
                JOptionPane.showMessageDialog(this, "Task added successfully!");
            }

            clearForm();
            mainFrame.refreshAll();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving task: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        currentTask = null;
        titleField.setText("");
        detailsArea.setText("");
        categoryCombo.setSelectedIndex(0);
        subjectField.setText("");
        topicField.setText("");
        tagField.setText("");
        dueDateField.setText(LocalDate.now().toString());
        prioritySpinner.setValue(3);
        toggleCategoryFields();
    }

    public void loadTask(Task task) {
        this.currentTask = task;
        titleField.setText(task.getTitle());
        detailsArea.setText(task.getDetails());
        categoryCombo.setSelectedItem(task.getCategory().getDisplay());

        if (task instanceof StudyTask st) {
            subjectField.setText(st.getSubject());
            topicField.setText(st.getTopic());
        } else if (task instanceof PersonalTask pt) {
            tagField.setText(pt.getTag());
        }

        dueDateField.setText(task.getDueDate().toString());
        prioritySpinner.setValue(task.getPriority());
        toggleCategoryFields();
    }
}
