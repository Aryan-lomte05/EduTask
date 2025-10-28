//package com.edutask.ui;
//
//import com.edutask.model.*;
//import com.edutask.service.TaskService;
//import com.edutask.ui.themes.PremiumTheme;
//import com.edutask.ui.themes.Icons;
//import com.edutask.util.*;
//import javax.swing.*;
//import java.awt.*;
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//
//public class TaskFormPanel extends JPanel {
//    private MainFrame mainFrame;
//    private TaskService taskService;
//
//    private JTextField titleField;
//    private JTextArea detailsArea;
//    private JComboBox<String> categoryCombo;
//    private JTextField subjectField;
//    private JTextField topicField;
//    private JTextField tagField;
//    private JTextField dueDateField;
//    private JSpinner timeSpinner;  // NEW: Time picker
//    private JSpinner prioritySpinner;
//
//    private Task currentTask;
//    private LocalTime selectedTime = LocalTime.of(12, 0);  // Default 12:00 PM
//
//    public TaskFormPanel(MainFrame mainFrame, TaskService taskService) {
//        this.mainFrame = mainFrame;
//        this.taskService = taskService;
//
//        setLayout(new BorderLayout());
//        setOpaque(false);
//        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//
//        initializeForm();
//    }
//
//    private void initializeForm() {
//        JPanel formPanel = new JPanel(new GridBagLayout());
//        formPanel.setOpaque(false);
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.insets = new Insets(8, 8, 8, 8);
//
//        // Title
//        addFormField(formPanel, gbc, 0, "Title:*", titleField = createTextField());
//
//        // Details
//        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
//        formPanel.add(new JLabel("Details:"), gbc);
//
//        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.3;
//        gbc.fill = GridBagConstraints.BOTH;
//        detailsArea = new JTextArea(4, 20);
//        detailsArea.setLineWrap(true);
//        detailsArea.setWrapStyleWord(true);
//        detailsArea.setFont(PremiumTheme.FONT_BODY);
//        JScrollPane detailsScroll = new JScrollPane(detailsArea);
//        styleTextField(detailsArea);
//        formPanel.add(detailsScroll, gbc);
//        gbc.weighty = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // Category
//        gbc.gridx = 0; gbc.gridy = 2;
//        formPanel.add(new JLabel("Category:*"), gbc);
//        gbc.gridx = 1;
//        categoryCombo = new JComboBox<>(new String[]{
//                "Study", "Personal", "Work", "Sports", "Health",
//                "Movies", "Games", "Travel", "Shopping", "Social", "Other"
//        });
//        categoryCombo.setFont(PremiumTheme.FONT_BODY);
//        categoryCombo.addActionListener(e -> toggleCategoryFields());
//        formPanel.add(categoryCombo, gbc);
//
//        // Subject
//        addFormField(formPanel, gbc, 3, "Subject:", subjectField = createTextField());
//
//        // Topic
//        addFormField(formPanel, gbc, 4, "Topic:", topicField = createTextField());
//
//        // Tag
//        addFormField(formPanel, gbc, 5, "Tag:", tagField = createTextField());
//        tagField.setEnabled(false);
//
//        // Due Date with Calendar Button
//        gbc.gridx = 0; gbc.gridy = 6;
//        formPanel.add(new JLabel("Due Date:*"), gbc);
//
//        gbc.gridx = 1;
//        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
//        datePanel.setOpaque(false);
//
//        dueDateField = new JTextField(LocalDate.now().toString());
//        dueDateField.setFont(PremiumTheme.FONT_BODY);
//        styleTextField(dueDateField);
//
//        JButton calendarBtn = new JButton(Icons.CALENDAR + " Pick");
//        PremiumTheme.styleButton(calendarBtn);
//        calendarBtn.addActionListener(e -> showDatePicker());
//
//        datePanel.add(dueDateField, BorderLayout.CENTER);
//        datePanel.add(calendarBtn, BorderLayout.EAST);
//        formPanel.add(datePanel, gbc);
//
//        // Time Picker (NEW!)
//        gbc.gridx = 0; gbc.gridy = 7;
//        formPanel.add(new JLabel("Due Time:"), gbc);
//
//        gbc.gridx = 1;
//        JPanel timePanel = new JPanel(new BorderLayout(5, 0));
//        timePanel.setOpaque(false);
//
//        SpinnerDateModel timeModel = new SpinnerDateModel();
//        timeSpinner = new JSpinner(timeModel);
//        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
//        timeSpinner.setEditor(timeEditor);
//        timeSpinner.setFont(PremiumTheme.FONT_BODY);
//
//        JButton clockBtn = new JButton(Icons.CLOCK + " Set");
//        PremiumTheme.styleButton(clockBtn);
//        clockBtn.addActionListener(e -> showTimePicker());
//
//        timePanel.add(timeSpinner, BorderLayout.CENTER);
//        timePanel.add(clockBtn, BorderLayout.EAST);
//        formPanel.add(timePanel, gbc);
//
//        // Priority
//        gbc.gridx = 0; gbc.gridy = 8;
//        formPanel.add(new JLabel("Priority:*"), gbc);
//
//        gbc.gridx = 1;
//        prioritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
//        prioritySpinner.setFont(PremiumTheme.FONT_BODY);
//        formPanel.add(prioritySpinner, gbc);
//
//        add(formPanel, BorderLayout.CENTER);
//
//        // Buttons
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
//        buttonPanel.setOpaque(false);
//
//        JButton saveBtn = new JButton(Icons.CHECK + " Save Task");
//        PremiumTheme.styleButton(saveBtn);
//        saveBtn.addActionListener(e -> saveTask());
//
//        JButton clearBtn = new JButton(Icons.CROSS + " Clear Form");
//        PremiumTheme.styleButton(clearBtn);
//        clearBtn.addActionListener(e -> clearForm());
//
//        buttonPanel.add(saveBtn);
//        buttonPanel.add(clearBtn);
//
//        add(buttonPanel, BorderLayout.SOUTH);
//    }
//
//    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
//        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
//        panel.add(new JLabel(label), gbc);
//        gbc.gridx = 1; gbc.weightx = 1.0;
//        panel.add(field, gbc);
//    }
//
//    private JTextField createTextField() {
//        JTextField field = new JTextField(20);
//        field.setFont(PremiumTheme.FONT_BODY);
//        styleTextField(field);
//        return field;
//    }
//
//    private void styleTextField(JComponent field) {
//        field.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1),
//                BorderFactory.createEmptyBorder(5, 8, 5, 8)
//        ));
//    }
//
//    private void showDatePicker() {
//        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
//        dialog.setLayout(new BorderLayout(10, 10));
//
//        JPanel calPanel = new JPanel(new GridLayout(7, 7, 5, 5));
//        calPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        LocalDate selected = LocalDate.parse(dueDateField.getText());
//        LocalDate firstDay = selected.withDayOfMonth(1);
//
//        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
//        for (String day : days) {
//            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
//            dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
//            calPanel.add(dayLabel);
//        }
//
//        int startDay = firstDay.getDayOfWeek().getValue() % 7;
//        for (int i = 0; i < startDay; i++) {
//            calPanel.add(new JLabel(""));
//        }
//
//        for (int day = 1; day <= selected.lengthOfMonth(); day++) {
//            JButton dayBtn = new JButton(String.valueOf(day));
//            final int finalDay = day;
//            dayBtn.addActionListener(e -> {
//                dueDateField.setText(selected.withDayOfMonth(finalDay).toString());
//                dialog.dispose();
//            });
//            calPanel.add(dayBtn);
//        }
//
//        dialog.add(calPanel, BorderLayout.CENTER);
//        dialog.pack();
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
//    }
//
//    private void showTimePicker() {
//        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Time", true);
//        dialog.setLayout(new BorderLayout(10, 10));
//        dialog.setSize(300, 250);
//
//        JPanel timePanel = new JPanel(new GridLayout(3, 1, 10, 10));
//        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
//        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
//        JComboBox<String> ampmCombo = new JComboBox<>(new String[]{"AM", "PM"});
//
//        timePanel.add(createSpinnerPanel("Hour:", hourSpinner));
//        timePanel.add(createSpinnerPanel("Minute:", minuteSpinner));
//        timePanel.add(createSpinnerPanel("Period:", ampmCombo));
//
//        JButton okBtn = new JButton("OK");
//        okBtn.addActionListener(e -> {
//            int hour = (Integer) hourSpinner.getValue();
//            int minute = (Integer) minuteSpinner.getValue();
//            boolean isPM = ampmCombo.getSelectedItem().equals("PM");
//
//            if (hour == 12) hour = 0;
//            if (isPM) hour += 12;
//
//            selectedTime = LocalTime.of(hour, minute);
//            timeSpinner.setValue(java.util.Date.from(
//                    LocalDateTime.of(LocalDate.now(), selectedTime)
//                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
//            dialog.dispose();
//        });
//
//        dialog.add(timePanel, BorderLayout.CENTER);
//        dialog.add(okBtn, BorderLayout.SOUTH);
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
//    }
//
//    private JPanel createSpinnerPanel(String label, JComponent component) {
//        JPanel panel = new JPanel(new BorderLayout(10, 0));
//        panel.add(new JLabel(label), BorderLayout.WEST);
//        panel.add(component, BorderLayout.CENTER);
//        return panel;
//    }
//
//    private void toggleCategoryFields() {
//        boolean isStudy = categoryCombo.getSelectedItem().equals("Study");
//        subjectField.setEnabled(isStudy);
//        topicField.setEnabled(isStudy);
//        tagField.setEnabled(!isStudy);
//    }
//
//    public void saveTask() {
//        try {
//            String title = titleField.getText().trim();
//            Validator.validateTaskTitle(title);
//
//            LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
//            Validator.validateDueDate(dueDate);
//
//            int priority = (Integer) prioritySpinner.getValue();
//            Validator.validatePriority(priority);
//
//            String details = detailsArea.getText().trim();
//            boolean isStudy = categoryCombo.getSelectedItem().equals("Study");
//
//            Task task;
//            if (isStudy) {
//                String subject = subjectField.getText().trim();
//                String topic = topicField.getText().trim();
//                Validator.validateStudyFields(subject, topic);
//
//                String id = currentTask != null ? currentTask.getId() : UUID.randomUUID().toString();
//                task = new StudyTask(id, title, details, dueDate, priority, subject, topic);
//            } else {
//                String tag = tagField.getText().trim();
//                if (tag.isEmpty()) tag = "General";
//
//                String id = currentTask != null ? currentTask.getId() : UUID.randomUUID().toString();
//                task = new PersonalTask(id, title, details, dueDate, priority, tag);
//            }
//
//            if (currentTask != null) {
//                task.setStatus(currentTask.getStatus());
//                taskService.updateTask(task);
//                JOptionPane.showMessageDialog(this, Icons.CHECK + " Task updated successfully!");
//            } else {
//                taskService.addTask(task);
//                JOptionPane.showMessageDialog(this, Icons.CHECK + " Task added to your board!");
//            }
//
//            clearForm();
//            mainFrame.refreshAll();
//
//        } catch (AppException ex) {
//            JOptionPane.showMessageDialog(this, Icons.CROSS + " " + ex.getMessage(),
//                    "Validation Error", JOptionPane.WARNING_MESSAGE);
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, Icons.CROSS + " Error: " + ex.getMessage(),
//                    "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    public void clearForm() {
//        currentTask = null;
//        titleField.setText("");
//        detailsArea.setText("");
//        categoryCombo.setSelectedIndex(0);
//        subjectField.setText("");
//        topicField.setText("");
//        tagField.setText("");
//        dueDateField.setText(LocalDate.now().toString());
//        selectedTime = LocalTime.of(12, 0);
//        prioritySpinner.setValue(3);
//        toggleCategoryFields();
//    }
//
//    public void loadTask(Task task) {
//        this.currentTask = task;
//        titleField.setText(task.getTitle());
//        detailsArea.setText(task.getDetails());
//        categoryCombo.setSelectedItem(task.getCategory().getDisplay());
//
//        if (task instanceof StudyTask st) {
//            subjectField.setText(st.getSubject());
//            topicField.setText(st.getTopic());
//        } else if (task instanceof PersonalTask pt) {
//            tagField.setText(pt.getTag());
//        }
//
//        dueDateField.setText(task.getDueDate().toString());
//        prioritySpinner.setValue(task.getPriority());
//        toggleCategoryFields();
//    }
//}
package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import com.edutask.ui.themes.Icons;
import com.edutask.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class TaskFormPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TaskService taskService;

    private JTextField titleField;
    private JTextArea detailsArea;
    private JComboBox<String> categoryCombo;
    private JTextField subjectField;
    private JTextField topicField;
    private JTextField tagField;
    private JTextField dueDateField;
    private JSpinner timeSpinner; // time picker
    private JSpinner prioritySpinner;

    private Task currentTask;
    private LocalTime selectedTime = LocalTime.of(12, 0);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public TaskFormPanel(MainFrame mainFrame, TaskService taskService) {
        this.mainFrame = mainFrame;
        this.taskService = taskService;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ---------- Header ----------

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Task Form");
        title.setFont(PremiumTheme.FONT_TITLE);
        title.setForeground(PremiumTheme.TEXT_PRIMARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(PremiumTheme.CORK_DARK);

        header.add(title, BorderLayout.NORTH);
        header.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        header.add(sep, BorderLayout.SOUTH);
        return header;
    }

    // ---------- Form Center ----------

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.add(buildFields(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFields() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(boxBorder());

        GridBagConstraints gbc = baseGbc();

        // Title
        addLabel(formPanel, gbc, 0, "Title:*");
        titleField = createTextField();
        addField(formPanel, gbc, 0, titleField);

        // Details
        addLabel(formPanel, gbc, 1, "Details:");
        detailsArea = new JTextArea(5, 22);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(PremiumTheme.FONT_BODY);
        styleArea(detailsArea);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(fieldBorder());
        addField(formPanel, gbc, 1, detailsScroll, GridBagConstraints.BOTH, 0.28);

        // Category
        addLabel(formPanel, gbc, 2, "Category:*");
        categoryCombo = new JComboBox<>(new String[]{
                "Study", "Personal", "Work", "Sports", "Health",
                "Movies", "Games", "Travel", "Shopping", "Social", "Other"
        });
        categoryCombo.setFont(PremiumTheme.FONT_BODY);
        styleCombo(categoryCombo);
        categoryCombo.addActionListener(e -> toggleCategoryFields());
        addField(formPanel, gbc, 2, categoryCombo);

        // Group: Study/Personal specifics
        // Subject
        addLabel(formPanel, gbc, 3, "Subject:");
        subjectField = createTextField();
        addField(formPanel, gbc, 3, subjectField);

        // Topic
        addLabel(formPanel, gbc, 4, "Topic:");
        topicField = createTextField();
        addField(formPanel, gbc, 4, topicField);

        // Tag
        addLabel(formPanel, gbc, 5, "Tag:");
        tagField = createTextField();
        tagField.setEnabled(false);
        addField(formPanel, gbc, 5, tagField);

        // Separator
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(sectionSeparator("Scheduling"), gbc);
        gbc.gridwidth = 1;

        // Due Date
        addLabel(formPanel, gbc, 7, "Due Date:*");
        JPanel datePanel = rowPanel();
        dueDateField = new JTextField(LocalDate.now().format(DATE_FMT));
        styleField(dueDateField);
        JButton pickDate = smallButton("Pick", e -> showDatePicker());
        datePanel.add(dueDateField, BorderLayout.CENTER);
        datePanel.add(pickDate, BorderLayout.EAST);
        addField(formPanel, gbc, 7, datePanel);

        // Due Time
        addLabel(formPanel, gbc, 8, "Due Time:");
        JPanel timePanel = rowPanel();
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setFont(PremiumTheme.FONT_BODY);
        setSpinnerToLocalTime(timeSpinner, selectedTime, LocalDate.now());
        styleSpinner(timeSpinner);
        JButton setTime = smallButton("Set", e -> showTimePicker());
        timePanel.add(timeSpinner, BorderLayout.CENTER);
        timePanel.add(setTime, BorderLayout.EAST);
        addField(formPanel, gbc, 8, timePanel);

        // Priority
        addLabel(formPanel, gbc, 9, "Priority:*");
        prioritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        prioritySpinner.setFont(PremiumTheme.FONT_BODY);
        styleSpinner(prioritySpinner);
        addField(formPanel, gbc, 9, prioritySpinner);

        toggleCategoryFields();
        return formPanel;
    }

    // ---------- Footer ----------

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(PremiumTheme.CORK_DARK);
        footer.add(sep, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);

        JButton saveBtn = primaryButton(Icons.CHECK + " Save Task", e -> saveTask());
        JButton clearBtn = neutralButton(Icons.CROSS + " Clear Form", e -> clearForm());

        actions.add(saveBtn);
        actions.add(clearBtn);

        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }

    // ---------- Interaction ----------

    private void toggleCategoryFields() {
        boolean isStudy = "Study".equals(String.valueOf(categoryCombo.getSelectedItem()));
        subjectField.setEnabled(isStudy);
        topicField.setEnabled(isStudy);
        tagField.setEnabled(!isStudy);

        Color on = Color.WHITE, off = new Color(245, 245, 245);
        subjectField.setBackground(isStudy ? on : off);
        topicField.setBackground(isStudy ? on : off);
        tagField.setBackground(!isStudy ? on : off);
    }

    private void showDatePicker() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel calPanel = new JPanel(new GridLayout(7, 7, 4, 4));
        calPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate base = parseDateSafe(dueDateField.getText(), LocalDate.now());
        LocalDate firstDay = base.withDayOfMonth(1);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel dayLabel = new JLabel(d, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
            calPanel.add(dayLabel);
        }

        int startOffset = firstDay.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startOffset; i++) calPanel.add(new JLabel(""));

        int last = base.lengthOfMonth();
        for (int day = 1; day <= last; day++) {
            JButton dayBtn = new JButton(String.valueOf(day));
            PremiumTheme.styleButton(dayBtn);
            int chosen = day;
            dayBtn.addActionListener(e -> {
                dueDateField.setText(base.withDayOfMonth(chosen).format(DATE_FMT));
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
        dialog.setSize(320, 240);

        JPanel timePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JSpinner hour = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
        JSpinner minute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
        JComboBox<String> ap = new JComboBox<>(new String[]{"AM", "PM"});

        timePanel.add(spinRow("Hour:", hour));
        timePanel.add(spinRow("Minute:", minute));
        timePanel.add(spinRow("Period:", ap));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton ok = smallButton("OK", e -> {
            int h = (Integer) hour.getValue();
            int m = (Integer) minute.getValue();
            boolean pm = "PM".equals(String.valueOf(ap.getSelectedItem()));
            if (h == 12) h = 0;
            if (pm) h += 12;
            selectedTime = LocalTime.of(h, m);
            setSpinnerToLocalTime(timeSpinner, selectedTime, LocalDate.now());
            dialog.dispose();
        });
        actions.add(ok);

        dialog.add(timePanel, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ---------- Save / Load / Clear ----------

    public void saveTask() {
        try {
            String title = titleField.getText().trim();
            Validator.validateTaskTitle(title);

            LocalDate dueDate = parseDateSafe(dueDateField.getText().trim(), LocalDate.now());
            Validator.validateDueDate(dueDate);

            int priority = (Integer) prioritySpinner.getValue();
            Validator.validatePriority(priority);

            boolean isStudy = "Study".equals(String.valueOf(categoryCombo.getSelectedItem()));
            String details = detailsArea.getText().trim();

            Task task;
            String id = (currentTask != null) ? currentTask.getId() : UUID.randomUUID().toString();

            if (isStudy) {
                String subject = subjectField.getText().trim();
                String topic = topicField.getText().trim();
                Validator.validateStudyFields(subject, topic);
                task = new StudyTask(id, title, details, dueDate, priority, subject, topic);
            } else {
                String tag = tagField.getText().trim();
                if (tag.isEmpty()) tag = "General";
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
        dueDateField.setText(LocalDate.now().format(DATE_FMT));
        selectedTime = LocalTime.of(12, 0);
        prioritySpinner.setValue(3);
        setSpinnerToLocalTime(timeSpinner, selectedTime, LocalDate.now());
        toggleCategoryFields();
    }

    public void loadTask(Task task) {
        currentTask = task;
        titleField.setText(task.getTitle());
        detailsArea.setText(task.getDetails());
        categoryCombo.setSelectedItem(task.getCategory().getDisplay());

        if (task instanceof StudyTask st) {
            subjectField.setText(st.getSubject());
            topicField.setText(st.getTopic());
        } else if (task instanceof PersonalTask pt) {
            tagField.setText(pt.getTag());
        }

        dueDateField.setText(task.getDueDate().format(DATE_FMT));
        prioritySpinner.setValue(task.getPriority());
        toggleCategoryFields();
    }

    // ---------- Small UI Helpers ----------

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;
        return gbc;
    }

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text);
        label.setFont(PremiumTheme.FONT_BODY);
        label.setForeground(PremiumTheme.TEXT_PRIMARY);
        p.add(label, gbc);
    }

    private void addField(JPanel p, GridBagConstraints gbc, int row, JComponent field) {
        addField(p, gbc, row, field, GridBagConstraints.HORIZONTAL, 0.0);
    }

    private void addField(JPanel p, GridBagConstraints gbc, int row, JComponent field, int fill, double weightY) {
        gbc.gridx = 1; gbc.gridy = row;
        gbc.weightx = 1.0; gbc.weighty = weightY;
        gbc.fill = fill;
        p.add(field, gbc);
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(PremiumTheme.FONT_BODY);
        styleField(tf);
        return tf;
    }

    private void styleField(JComponent field) {
        field.setBorder(fieldBorder());
        field.setBackground(Color.WHITE);
        field.setForeground(PremiumTheme.TEXT_PRIMARY);
        field.setOpaque(true);
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        );
    }

    private Border boxBorder() {
        return new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, PremiumTheme.CORK_DARK),
                new EmptyBorder(10, 10, 10, 10)
        );
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBorder(BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1));
        combo.setBackground(Color.WHITE);
        combo.setForeground(PremiumTheme.TEXT_PRIMARY);
    }

    private void styleArea(JTextArea area) {
        area.setBackground(Color.WHITE);
        area.setForeground(PremiumTheme.TEXT_PRIMARY);
    }

    private JPanel rowPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        return p;
    }

    private JButton smallButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        PremiumTheme.styleButton(btn);
        btn.addActionListener(action);
        return btn;
    }

    private JButton primaryButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        if (hasMethod(PremiumTheme.class, "stylePrimaryButton", JButton.class)) {
            try { PremiumTheme.class.getMethod("stylePrimaryButton", JButton.class).invoke(null, btn); }
            catch (Exception ignored) { PremiumTheme.styleButton(btn); }
        } else {
            PremiumTheme.styleButton(btn);
        }
        btn.addActionListener(action);
        return btn;
    }

    private JButton neutralButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        PremiumTheme.styleButton(btn);
        btn.addActionListener(action);
        return btn;
    }

    private JPanel sectionSeparator(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(PremiumTheme.FONT_BODY);
        l.setForeground(PremiumTheme.TEXT_SECONDARY);
        JSeparator s = new JSeparator();
        s.setForeground(PremiumTheme.CORK_DARK);
        p.add(l, BorderLayout.WEST);
        p.add(s, BorderLayout.CENTER);
        return p;
    }

    private JPanel spinRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBorder(BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1));
    }

    private void setSpinnerToLocalTime(JSpinner spinner, LocalTime time, LocalDate date) {
        ZonedDateTime zdt = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault());
        spinner.setValue(Date.from(zdt.toInstant()));
    }

    private LocalDate parseDateSafe(String text, LocalDate fallback) {
        try { return LocalDate.parse(text, DATE_FMT); } catch (Exception ex) { return fallback; }
    }

    private boolean hasMethod(Class<?> cls, String name, Class<?>... sig) {
        try { cls.getMethod(name, sig); return true; } catch (Exception e) { return false; }
    }
}
