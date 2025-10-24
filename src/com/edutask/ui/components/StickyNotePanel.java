package com.edutask.ui.components;

import com.edutask.model.*;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;

public class StickyNotePanel extends JPanel {
    private Task task;
    private boolean isHovered = false;
    private boolean isSelected = false;

    public StickyNotePanel(Task task) {
        this.task = task;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setPreferredSize(new Dimension(220, 180));
        setMaximumSize(new Dimension(220, 180));
        setBorder(BorderFactory.createEmptyBorder(15, 12, 12, 12));

        initializeContent();
        addHoverEffect();
    }

    private void initializeContent() {
        // Title area with pin space
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));

        JLabel titleLabel = new JLabel(truncate(task.getTitle(), 25));
        titleLabel.setFont(PremiumTheme.FONT_HANDWRITING);
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Subject/Topic (for study tasks)
        if (task instanceof StudyTask st) {
            JLabel subjectLabel = new JLabel("📚 " + st.getSubject() + " - " + st.getTopic());
            subjectLabel.setFont(PremiumTheme.FONT_SMALL);
            subjectLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
            contentPanel.add(subjectLabel);
            contentPanel.add(Box.createVerticalStrut(5));
        }

        // Due date
        JLabel dueDateLabel = new JLabel("📅 " + com.edutask.util.DateUtils.getDueLabel(task.getDueDate()));
        dueDateLabel.setFont(PremiumTheme.FONT_SMALL);
        dueDateLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
        contentPanel.add(dueDateLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        // Priority stars
        JLabel priorityLabel = new JLabel("⭐ " + "★".repeat(task.getPriority()));
        priorityLabel.setFont(PremiumTheme.FONT_SMALL);
        priorityLabel.setForeground(getPriorityColor());
        contentPanel.add(priorityLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        // Status badge
        JLabel statusLabel = new JLabel(getStatusEmoji() + " " + task.getStatus().getDisplay());
        statusLabel.setFont(PremiumTheme.FONT_SMALL);
        statusLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
        contentPanel.add(statusLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Shadow (slight offset)
        if (isHovered) {
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(5, 5, width - 10, height - 10, 8, 8);
        } else {
            g2.setColor(PremiumTheme.SHADOW);
            g2.fillRoundRect(3, 3, width - 6, height - 6, 8, 8);
        }

        // Sticky note background
        Color noteColor = PremiumTheme.getStickyColorByPriority(task.getPriority());
        g2.setColor(noteColor);
        g2.fillRoundRect(0, 0, width - 5, height - 5, 8, 8);

        // Border
        g2.setColor(noteColor.darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, width - 6, height - 6, 8, 8);

        // Selection highlight
        if (isSelected) {
            g2.setColor(new Color(255, 100, 0, 100));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, width - 10, height - 10, 8, 8);
        }

        // Draw pin at top center
        drawPin(g2, width / 2, 8);

        g2.dispose();
    }

    private void drawPin(Graphics2D g2, int x, int y) {
        // Pin head (circle)
        g2.setColor(PremiumTheme.PIN_HEAD);
        g2.fillOval(x - 5, y - 5, 10, 10);

        // Pin shadow on head
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillArc(x - 5, y - 3, 10, 10, 180, 180);

        // Pin needle (small line)
        g2.setColor(PremiumTheme.PIN_METAL);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x, y + 5, x, y + 12);
    }

    private void addHoverEffect() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public Task getTask() {
        return task;
    }

    private Color getPriorityColor() {
        return switch (task.getPriority()) {
            case 5, 4 -> PremiumTheme.PRIORITY_HIGH;
            case 3 -> PremiumTheme.PRIORITY_MEDIUM;
            default -> PremiumTheme.PRIORITY_LOW;
        };
    }

    private String getStatusEmoji() {
        return switch (task.getStatus()) {
            case COMPLETED -> "✅";
            case IN_PROGRESS -> "⏳";
            default -> "📌";
        };
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
