package com.edutask.ui.calendar;

import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;

public class CalendarViewPanel extends JPanel {
    private TaskService taskService;
    private CardLayout cardLayout;
    private JPanel viewContainer;

    // Different calendar views
    private YearlyCalendarView yearlyView;
    private MonthlyCalendarView monthlyView;
    private WeeklyGanttView weeklyView;
    private DailyTimelineView dailyView;

    private JLabel currentViewLabel;

    public CalendarViewPanel(TaskService taskService) {
        this.taskService = taskService;

        setLayout(new BorderLayout());
        setOpaque(false);

        initializeViews();
        createNavigationBar();

        cardLayout.show(viewContainer, "YEARLY");
    }

    private void initializeViews() {
        cardLayout = new CardLayout();
        viewContainer = new JPanel(cardLayout);
        viewContainer.setOpaque(false);

        yearlyView = new YearlyCalendarView(taskService, this);
        monthlyView = new MonthlyCalendarView(taskService, this);
        weeklyView = new WeeklyGanttView(taskService, this);
        dailyView = new DailyTimelineView(taskService, this);

        viewContainer.add(yearlyView, "YEARLY");
        viewContainer.add(monthlyView, "MONTHLY");
        viewContainer.add(weeklyView, "WEEKLY");
        viewContainer.add(dailyView, "DAILY");

        add(viewContainer, BorderLayout.CENTER);
    }

    private void createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setOpaque(false);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: View buttons
        JPanel viewButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        viewButtons.setOpaque(false);

        JButton yearBtn = createNavButton("📅 Year", "YEARLY");
        JButton monthBtn = createNavButton("📆 Month", "MONTHLY");
        JButton weekBtn = createNavButton("📊 Week", "WEEKLY");
        JButton dayBtn = createNavButton("🕐 Day", "DAILY");

        viewButtons.add(yearBtn);
        viewButtons.add(monthBtn);
        viewButtons.add(weekBtn);
        viewButtons.add(dayBtn);

        // Center: Current view label
        currentViewLabel = new JLabel("Yearly View - 2025", SwingConstants.CENTER);
        currentViewLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentViewLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        navBar.add(viewButtons, BorderLayout.WEST);
        navBar.add(currentViewLabel, BorderLayout.CENTER);

        add(navBar, BorderLayout.NORTH);
    }

    private JButton createNavButton(String text, String view) {
        JButton btn = new JButton(text);
        PremiumTheme.styleButton(btn);
        btn.addActionListener(e -> switchToView(view));
        return btn;
    }

    public void switchToView(String viewName) {
        cardLayout.show(viewContainer, viewName);
        updateLabel(viewName);
    }

    public void switchToMonth(int year, int month) {
        monthlyView.setYearMonth(year, month);
        switchToView("MONTHLY");
    }

    public void switchToWeek(int year, int month, int weekNumber) {
        weeklyView.setWeek(year, month, weekNumber);
        switchToView("WEEKLY");
    }

    public void switchToDay(int year, int month, int day) {
        dailyView.setDate(year, month, day);
        switchToView("DAILY");
    }

    private void updateLabel(String viewName) {
        String label = switch (viewName) {
            case "YEARLY" -> "Yearly View - 2025";
            case "MONTHLY" -> "Monthly View";
            case "WEEKLY" -> "Weekly View";
            case "DAILY" -> "Daily View";
            default -> "Calendar View";
        };
        currentViewLabel.setText(label);
    }

    public void refresh() {
        yearlyView.refresh();
        monthlyView.refresh();
        weeklyView.refresh();
        dailyView.refresh();
    }
}
