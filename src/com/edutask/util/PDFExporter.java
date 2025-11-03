package com.edutask.util;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.awt.Desktop;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PDFExporter {
    // Premium Fonts
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new BaseColor(41, 128, 185));
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(52, 73, 94));
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
    private static final Font TIME_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(231, 76, 60));

    // Premium Colors
    private static final BaseColor HEADER_BG = new BaseColor(52, 73, 94);
    private static final BaseColor ALT_ROW = new BaseColor(236, 240, 241);
    private static final BaseColor BORDER_COLOR = new BaseColor(189, 195, 199);

    public static void showExportDialog(TaskService taskService, JFrame parent) {
        String[] options = {"Daily Planner", "Weekly Planner", "Monthly Calendar", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent,
                "Choose PDF Export Type:",
                "Export Tasks to PDF",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        switch (choice) {
            case 0 -> exportDailyPlanner(taskService, parent);
            case 1 -> exportWeeklyPlanner(taskService, parent);
            case 2 -> exportMonthlyCalendar(taskService, parent);
        }
    }

    private static void exportDailyPlanner(TaskService taskService, JFrame parent) {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setValue(new Date());

        int result = JOptionPane.showConfirmDialog(parent, dateSpinner,
                "Select Date for Daily Planner:", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date date = (Date) dateSpinner.getValue();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("EduTask_Daily_" + localDate + ".pdf"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    generateDailyPDF(taskService, localDate, fc.getSelectedFile());
                    showSuccess(parent, fc.getSelectedFile());
                } catch (Exception e) {
                    showError(parent, e);
                }
            }
        }
    }

    private static void exportWeeklyPlanner(TaskService taskService, JFrame parent) {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setValue(new Date());

        int result = JOptionPane.showConfirmDialog(parent, dateSpinner,
                "Select Week Start Date (Monday):", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date date = (Date) dateSpinner.getValue();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate weekStart = localDate.with(java.time.DayOfWeek.MONDAY);

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("EduTask_Weekly_" + weekStart + ".pdf"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    generateWeeklyPDF(taskService, weekStart, fc.getSelectedFile());
                    showSuccess(parent, fc.getSelectedFile());
                } catch (Exception e) {
                    showError(parent, e);
                }
            }
        }
    }

    private static void exportMonthlyCalendar(TaskService taskService, JFrame parent) {
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 1, 5, 5));
        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2030, 1));

        panel.add(new JLabel("Month:"));
        panel.add(monthCombo);
        panel.add(new JLabel("Year:"));
        panel.add(yearSpinner);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Select Month & Year:", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            int month = monthCombo.getSelectedIndex() + 1;
            int year = (Integer) yearSpinner.getValue();
            YearMonth yearMonth = YearMonth.of(year, month);

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("EduTask_Monthly_" + yearMonth + ".pdf"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    generateMonthlyPDF(taskService, yearMonth, fc.getSelectedFile());
                    showSuccess(parent, fc.getSelectedFile());
                } catch (Exception e) {
                    showError(parent, e);
                }
            }
        }
    }

    private static void generateDailyPDF(TaskService taskService, LocalDate date, File file) throws Exception {
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Header with gradient effect (simulated with table)
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(HEADER_BG);
        headerCell.setPadding(15);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("DAILY TASK PLANNER", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        Paragraph subtitle = new Paragraph(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.WHITE));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(subtitle);

        headerTable.addCell(headerCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));

        List<Task> tasks = taskService.getTasksByDate(date);

        if (tasks.isEmpty()) {
            Paragraph empty = new Paragraph("No tasks scheduled for this day. Enjoy your free time!", BODY_FONT);
            empty.setAlignment(Element.ALIGN_CENTER);
            empty.setSpacingBefore(30);
            document.add(empty);
        } else {
            // Timeline view (hour by hour)
            PdfPTable timeline = new PdfPTable(new float[]{1f, 6f});
            timeline.setWidthPercentage(100);
            timeline.setSpacingBefore(10);

            // Sort by time
            tasks.sort(Comparator.comparing(t -> safeGetTime(t)));

            for (Task task : tasks) {
                // Time cell
                PdfPCell timeCell = new PdfPCell();
                timeCell.setBackgroundColor(new BaseColor(230, 126, 34));
                timeCell.setPadding(10);
                timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                Paragraph timeP = new Paragraph(safeGetTime(task).format(DateTimeFormatter.ofPattern("hh:mm a")),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE));
                timeCell.addElement(timeP);
                timeline.addCell(timeCell);

                // Task details cell
                PdfPCell taskCell = new PdfPCell();
                taskCell.setPadding(10);
                taskCell.setBackgroundColor(getPriorityColor(task.getPriority()));

                Paragraph taskTitle = new Paragraph(task.getTitle(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK));
                taskCell.addElement(taskTitle);

                if (task.getDetails() != null && !task.getDetails().isEmpty()) {
                    Paragraph taskDetails = new Paragraph(truncate(task.getDetails(), 80), SMALL_FONT);
                    taskCell.addElement(taskDetails);
                }

                Paragraph meta = new Paragraph(
                        task.getCategory().getDisplay() + " | Priority: " + "*".repeat(task.getPriority()),
                        SMALL_FONT
                );
                taskCell.addElement(meta);

                timeline.addCell(taskCell);
            }

            document.add(timeline);
        }

        // Footer
        addFooter(document);
        document.close();
    }

    private static void generateWeeklyPDF(TaskService taskService, LocalDate weekStart, File file) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Header
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(41, 128, 185));
        headerCell.setPadding(15);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("WEEKLY TASK PLANNER", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        LocalDate weekEnd = weekStart.plusDays(6);
        Paragraph subtitle = new Paragraph(
                weekStart.format(DateTimeFormatter.ofPattern("MMM dd")) + " - " +
                        weekEnd.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.WHITE)
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(subtitle);

        headerTable.addCell(headerCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));

        // Week grid
        PdfPTable weekTable = new PdfPTable(7);
        weekTable.setWidthPercentage(100);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            PdfPCell dayHeader = new PdfPCell(new Phrase(day, HEADER_FONT));
            dayHeader.setBackgroundColor(HEADER_BG);
            dayHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            dayHeader.setPadding(8);
            weekTable.addCell(dayHeader);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            List<Task> dayTasks = taskService.getTasksByDate(date);

            PdfPCell dayCell = new PdfPCell();
            dayCell.setMinimumHeight(120);
            dayCell.setPadding(8);
            dayCell.setBackgroundColor(date.equals(LocalDate.now()) ? new BaseColor(255, 250, 205) : BaseColor.WHITE);

            Paragraph dateLabel = new Paragraph(date.format(DateTimeFormatter.ofPattern("dd")), SUBTITLE_FONT);
            dayCell.addElement(dateLabel);

            if (!dayTasks.isEmpty()) {
                dayCell.addElement(new Paragraph(" "));
                for (Task task : dayTasks) {
                    Paragraph taskP = new Paragraph(
                            "• " + safeGetTime(task).format(DateTimeFormatter.ofPattern("HH:mm")) + " " + truncate(task.getTitle(), 15),
                            SMALL_FONT
                    );
                    dayCell.addElement(taskP);
                }
            }

            weekTable.addCell(dayCell);
        }

        document.add(weekTable);
        addFooter(document);
        document.close();
    }

    private static void generateMonthlyPDF(TaskService taskService, YearMonth month, File file) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Header
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(155, 89, 182));
        headerCell.setPadding(15);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("MONTHLY CALENDAR", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        Paragraph subtitle = new Paragraph(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.WHITE));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(subtitle);

        headerTable.addCell(headerCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));

        PdfPTable calendar = new PdfPTable(7);
        calendar.setWidthPercentage(100);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            PdfPCell dayHeader = new PdfPCell(new Phrase(day, HEADER_FONT));
            dayHeader.setBackgroundColor(new BaseColor(52, 73, 94));
            dayHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            dayHeader.setPadding(8);
            calendar.addCell(dayHeader);
        }

        LocalDate firstDay = month.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < startDayOfWeek; i++) {
            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBackgroundColor(new BaseColor(245, 245, 245));
            calendar.addCell(emptyCell);
        }

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            List<Task> dayTasks = taskService.getTasksByDate(date);

            PdfPCell dayCell = new PdfPCell();
            dayCell.setMinimumHeight(80);
            dayCell.setPadding(6);
            dayCell.setBackgroundColor(date.equals(LocalDate.now()) ? new BaseColor(255, 250, 205) : BaseColor.WHITE);

            Paragraph dayNum = new Paragraph(String.valueOf(day),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(41, 128, 185)));
            dayCell.addElement(dayNum);

            if (!dayTasks.isEmpty()) {
                Paragraph count = new Paragraph(dayTasks.size() + " task(s)",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(231, 76, 60)));
                dayCell.addElement(count);

                for (int i = 0; i < Math.min(2, dayTasks.size()); i++) {
                    Paragraph taskP = new Paragraph("• " + truncate(dayTasks.get(i).getTitle(), 18), SMALL_FONT);
                    dayCell.addElement(taskP);
                }
            }

            calendar.addCell(dayCell);
        }

        document.add(calendar);
        addFooter(document);
        document.close();
    }

    private static LocalTime safeGetTime(Task task) {
        LocalTime time = task.getDueTime();
        return (time != null) ? time : LocalTime.of(12, 0);
    }

    private static BaseColor getPriorityColor(int priority) {
        return switch (priority) {
            case 5 -> new BaseColor(255, 220, 220); // Red
            case 4 -> new BaseColor(255, 235, 220); // Orange
            case 3 -> new BaseColor(255, 255, 220); // Yellow
            case 2 -> new BaseColor(220, 255, 220); // Green
            default -> new BaseColor(240, 248, 255); // Blue
        };
    }

    private static void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell footerCell = new PdfPCell(new Phrase(
                "Generated by EduTask Manager - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                SMALL_FONT
        ));
        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerCell.setBorder(Rectangle.TOP);
        footerCell.setPadding(8);
        footer.addCell(footerCell);
        document.add(footer);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 2) + "..";
    }

    private static void showSuccess(JFrame parent, File file) {
        JOptionPane.showMessageDialog(parent,
                "PDF exported successfully!\n\nSaved to: " + file.getAbsolutePath(),
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {}
    }

    private static void showError(JFrame parent, Exception e) {
        JOptionPane.showMessageDialog(parent,
                "Error exporting PDF: " + e.getMessage(),
                "Export Failed", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
