package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.QuizService;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class QuizDialog extends JDialog {
    private StudyTask task;
    private QuizService quizService;
    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;

    private JLabel questionLabel;
    private ButtonGroup optionGroup;
    private JPanel optionsPanel;
    private JButton nextBtn;
    private JButton submitBtn;
    private JLabel progressLabel;

    public QuizDialog(Frame parent, StudyTask task, QuizService quizService) {
        super(parent, "Quiz: " + task.getSubject() + " - " + task.getTopic(), true);

        this.task = task;
        this.quizService = quizService;
        this.questions = quizService.getQuestions(task.getSubject(), task.getTopic(), 5);

        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No quiz questions available for this topic yet!",
                    "No Questions", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        initializeUI();
        loadQuestion();

        setSize(600, 450);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // Progress
        progressLabel = new JLabel();
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(progressLabel, BorderLayout.NORTH);

        // Question + Options
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        questionLabel.setVerticalAlignment(SwingConstants.TOP);
        JScrollPane questionScroll = new JScrollPane(questionLabel);
        questionScroll.setPreferredSize(new Dimension(0, 100));
        centerPanel.add(questionScroll, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        centerPanel.add(optionsPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        nextBtn = new JButton("Next Question →");
        nextBtn.addActionListener(e -> nextQuestion());

        submitBtn = new JButton("Submit Quiz");
        submitBtn.addActionListener(e -> submitQuiz());
        submitBtn.setVisible(false);

        buttonPanel.add(nextBtn);
        buttonPanel.add(submitBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadQuestion() {
        QuizQuestion q = questions.get(currentIndex);

        progressLabel.setText(String.format("Question %d of %d  |  Subject: %s  |  Topic: %s",
                currentIndex + 1, questions.size(),
                q.getSubject(), q.getTopic()));

        questionLabel.setText("<html>" + q.getPrompt() + "</html>");

        optionsPanel.removeAll();
        optionGroup = new ButtonGroup();

        for (String option : q.getOptions()) {
            JRadioButton btn = new JRadioButton(option);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            optionGroup.add(btn);
            optionsPanel.add(btn);
            optionsPanel.add(Box.createVerticalStrut(5));
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();

        // Show submit button on last question
        if (currentIndex == questions.size() - 1) {
            nextBtn.setVisible(false);
            submitBtn.setVisible(true);
        }
    }

    private void nextQuestion() {
        if (checkAnswer()) {
            score++;
        }

        currentIndex++;
        if (currentIndex < questions.size()) {
            loadQuestion();
        }
    }

    private boolean checkAnswer() {
        QuizQuestion q = questions.get(currentIndex);

        Enumeration<AbstractButton> buttons = optionGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton btn = buttons.nextElement();
            if (btn.isSelected()) {
                return btn.getText().equals(q.getAnswerKey());
            }
        }
        return false;
    }

    private void submitQuiz() {
        if (checkAnswer()) {
            score++;
        }

        double percentage = (score * 100.0) / questions.size();
        String grade = percentage >= 80 ? "Excellent! 🌟" :
                percentage >= 60 ? "Good! 👍" : "Keep practicing! 📚";

        String message = String.format("""
            <html>
            <h2>Quiz Completed!</h2>
            <p><b>Score:</b> %d / %d (%.1f%%)</p>
            <p><b>Grade:</b> %s</p>
            <p>Review your notes and try again to improve!</p>
            </html>
            """, score, questions.size(), percentage, grade);

        JOptionPane.showMessageDialog(this, message, "Quiz Results",
                JOptionPane.INFORMATION_MESSAGE);

        // Save session (simplified)
        try {
            String sessionId = UUID.randomUUID().toString();
            QuizSession session = new QuizSession(sessionId, task.getId(), score, questions.size());
            // You could save this via taskService if needed
        } catch (Exception ex) {
            System.err.println("Error saving quiz session: " + ex.getMessage());
        }

        dispose();
    }
}
