package com.edutask.ui;

import javax.swing.*;
import java.awt.*;

public class FilterPanel extends JPanel {
    private MainFrame mainFrame;

    private JComboBox<String> categoryCombo;
    private JComboBox<String> statusCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<String> dueCombo;

    public FilterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeFilters();
    }

    private void initializeFilters() {
        add(createLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"All", "Study", "Personal"});
        add(categoryCombo);
        add(Box.createVerticalStrut(10));

        add(createLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});
        add(statusCombo);
        add(Box.createVerticalStrut(10));

        add(createLabel("Min Priority:"));
        priorityCombo = new JComboBox<>(new String[]{"All", "1", "2", "3", "4", "5"});
        add(priorityCombo);
        add(Box.createVerticalStrut(10));

        add(createLabel("Due Date:"));
        dueCombo = new JComboBox<>(new String[]{"All", "Today", "This Week", "Overdue"});
        add(dueCombo);
        add(Box.createVerticalStrut(15));

        JButton applyBtn = new JButton("Apply Filters");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBtn.addActionListener(e -> applyFilters());
        add(applyBtn);

        add(Box.createVerticalStrut(10));

        JButton resetBtn = new JButton("Reset");
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> resetFilters());
        add(resetBtn);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private void applyFilters() {
        String category = (String) categoryCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        String due = (String) dueCombo.getSelectedItem();

        mainFrame.applyFilters(category, status, priority, due);
    }

    private void resetFilters() {
        categoryCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        priorityCombo.setSelectedIndex(0);
        dueCombo.setSelectedIndex(0);
        applyFilters();
    }
}
