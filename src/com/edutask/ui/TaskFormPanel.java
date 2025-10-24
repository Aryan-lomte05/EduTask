package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import com.edutask.ui.themes.Icons;
import com.edutask.util.*;
import javax.swing.*;
import java.awt.*;
import java.time.*;
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
    private JSpinner timeSpinner;  // NEW: Time picker
    private JSpinner prioritySpinner;

    private Task currentTask;
    private LocalTime selectedTime = LocalTime.of(12, 0);  // Default 12:00 PM

    public TaskFormPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeForm();
    }

    private void initializeForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        addFormField(formPanel, gbc, 0, "Title:*", titleField = createTextField());

        // Details
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Details:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        detailsArea = new JTextArea(4, 20);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(PremiumTheme.FONT_BODY);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        styleTextField(detailsArea);
        formPanel.add(detailsScroll, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Category:*"), gbc);
        gbc.gridx = 1;
        categoryCombo = new JComboBox<>(new String[]{
                "Study", "Personal", "Work", "Sports", "Health",
                "Movies", "Games", "Travel", "Shopping", "Social", "Other"
        });
        categoryCombo.setFont(PremiumTheme.FONT_BODY);
        categoryCombo.addActionListener(e -> toggleCategoryFields());
        formPanel.add(categoryCombo, gbc);

        // Subject
        addFormField(formPanel, gbc, 3, "Subject:", subjectField = createTextField());

        // Topic
        addFormField(formPanel, gbc, 4, "Topic:", topicField = createTextField());

        // Tag
        addFormField(formPanel, gbc, 5, "Tag:", tagField = createTextField());
        tagField.setEnabled(false);

        // Due Date with Calendar Button
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Due Date:*"), gbc);

        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setOpaque(false);

        dueDateField = new JTextField(LocalDate.now().toString());
        dueDateField.setFont(PremiumTheme.FONT_BODY);
        styleTextField(dueDateField);

        JButton calendarBtn = new JButton(Icons.CALENDAR + " Pick");
        PremiumTheme.styleButton(calendarBtn);
        calendarBtn.addActionListener(e -> showDatePicker());

        datePanel.add(dueDateField, BorderLayout.CENTER);
        datePanel.add(calendarBtn, BorderLayout.EAST);
        formPanel.add(datePanel, gbc);

        // Time Picker (NEW!)
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Due Time:"), gbc);

        gbc.gridx = 1;
        JPanel timePanel = new JPanel(new BorderLayout(5, 0));
        timePanel.setOpaque(false);

        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setFont(PremiumTheme.FONT_BODY);

        JButton clockBtn = new JButton(Icons.CLOCK + " Set");
        PremiumTheme.styleButton(clockBtn);
        clockBtn.addActionListener(e -> showTimePicker());

        timePanel.add(timeSpinner, BorderLayout.CENTER);
        timePanel.add(clockBtn, BorderLayout.EAST);
        formPanel.add(timePanel, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Priority:*"), gbc);

        gbc.gridx = 1;
        prioritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        prioritySpinner.setFont(PremiumTheme.FONT_BODY);
        formPanel.add(prioritySpinner, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setOpaque(false);

        JButton saveBtn = new JButton(Icons.CHECK + " Save Task");
        PremiumTheme.styleButton(saveBtn);
        saveBtn.addActionListener(e -> saveTask());

        JButton clearBtn = new JButton(Icons.CROSS + " Clear Form");
        PremiumTheme.styleButton(clearBtn);
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(PremiumTheme.FONT_BODY);
        styleTextField(field);
        return field;
    }

    private void styleTextField(JComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void showDatePicker() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel calPanel = new JPanel(new GridLayout(7, 7, 5, 5));
        calPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate selected = LocalDate.parse(dueDateField.getText());
        LocalDate firstDay = selected.withDayOfMonth(1);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
            calPanel.add(dayLabel);
        }

        int startDay = firstDay.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startDay; i++) {
            calPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= selected.lengthOfMonth(); day++) {
            JButton dayBtn = new JButton(String.valueOf(day));
            final int finalDay = day;
            dayBtn.addActionListener(e -> {
                dueDateField.setText(selected.withDayOfMonth(finalDay).toString());
                dialog.dispose();
            });
            calPanel.add(dayBtn);
        }

        dialog.add(calPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showTimePicker() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Time", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(300, 250);

        JPanel timePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
        JComboBox<String> ampmCombo = new JComboBox<>(new String[]{"AM", "PM"});

        timePanel.add(createSpinnerPanel("Hour:", hourSpinner));
        timePanel.add(createSpinnerPanel("Minute:", minuteSpinner));
        timePanel.add(createSpinnerPanel("Period:", ampmCombo));

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            boolean isPM = ampmCombo.getSelectedItem().equals("PM");

            if (hour == 12) hour = 0;
            if (isPM) hour += 12;

            selectedTime = LocalTime.of(hour, minute);
            timeSpinner.setValue(java.util.Date.from(
                    LocalDateTime.of(LocalDate.now(), selectedTime)
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
            dialog.dispose();
        });

        dialog.add(timePanel, BorderLayout.CENTER);
        dialog.add(okBtn, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSpinnerPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void toggleCategoryFields() {
        boolean isStudy = categoryCombo.getSelectedItem().equals("Study");
        subjectField.setEnabled(isStudy);
        topicField.setEnabled(isStudy);
        tagField.setEnabled(!isStudy);
    }

    public void saveTask() {
        try {
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
                JOptionPane.showMessageDialog(this, Icons.CHECK + " Task updated successfully!");
            } else {
                taskService.addTask(task);
                JOptionPane.showMessageDialog(this, Icons.CHECK + " Task added to your board!");
            }

            clearForm();
            mainFrame.refreshAll();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, Icons.CROSS + " " + ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, Icons.CROSS + " Error: " + ex.getMessage(),
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
        selectedTime = LocalTime.of(12, 0);
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
