package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.ui.themes.PremiumTheme;
import com.edutask.audio.SoundManager;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import com.edutask.ai.AIQuizGenerator;
public class QuizDialog extends JDialog {
    private List<QuizQuestion> questions;
    private int currentQuestion = 0;
    private Map<Integer, List<Integer>> userAnswers = new HashMap<>();
    private int score = 0;
    private int timeRemaining; // in seconds
    private Timer quizTimer;

    private JLabel questionLabel;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private JPanel optionsPanel;
    private JButton nextButton;
    private JButton submitButton;

    public QuizDialog(Frame parent, String subject, String topic, int numQuestions) {
        super(parent, "Advanced Quiz - " + subject + ": " + topic, true);

        // Generate quiz
        com.edutask.ai.AIQuizGenerator generator = new com.edutask.ai.AIQuizGenerator();
        this.questions = generator.generateQuiz(subject, topic, numQuestions);
        this.timeRemaining = numQuestions * 60; // 1 minute per question

        initializeUI();
        startTimer();
        loadQuestion();

        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));

        // Top panel: Timer and Progress
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(100, 150, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        timerLabel = new JLabel("Time: " + formatTime(timeRemaining));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setForeground(Color.WHITE);

        progressLabel = new JLabel("Question 1/" + questions.size());
        progressLabel.setFont(new Font("Arial", Font.BOLD, 18));
        progressLabel.setForeground(Color.WHITE);

        topPanel.add(timerLabel, BorderLayout.WEST);
        topPanel.add(progressLabel, BorderLayout.EAST);

        // Center panel: Question and Options
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(Color.WHITE);

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setVerticalAlignment(SwingConstants.TOP);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setBorder(null);

        centerPanel.add(questionLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel: Navigation
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(Color.WHITE);

        nextButton = new JButton("Next Question");
        submitButton = new JButton("Submit Quiz");

        PremiumTheme.styleButton(nextButton);
        PremiumTheme.styleButton(submitButton);

        nextButton.addActionListener(e -> nextQuestion());
        submitButton.addActionListener(e -> submitQuiz());

        submitButton.setVisible(false);

        bottomPanel.add(nextButton);
        bottomPanel.add(submitButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadQuestion() {
        if (currentQuestion >= questions.size()) {
            submitQuiz();
            return;
        }

        QuizQuestion q = questions.get(currentQuestion);
        optionsPanel.removeAll();

        // Update labels
        questionLabel.setText("<html><b>Q" + (currentQuestion + 1) + ".</b> " + q.getQuestion() + "</html>");
        progressLabel.setText("Question " + (currentQuestion + 1) + "/" + questions.size());

        // Show appropriate input based on question type
        switch (q.getType()) {
            case MCQ -> loadMCQOptions(q);
            case MMCQ -> loadMMCQOptions(q);
            case NUMERICAL -> loadNumericalInput(q);
            case TRUE_FALSE -> loadTrueFalseOptions(q);
        }

        // Show submit button on last question
        if (currentQuestion == questions.size() - 1) {
            nextButton.setVisible(false);
            submitButton.setVisible(true);
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void loadMCQOptions(QuizQuestion q) {
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttons = new ArrayList<>();

        for (int i = 0; i < q.getOptions().size(); i++) {
            JRadioButton radio = new JRadioButton(q.getOptions().get(i));
            radio.setFont(new Font("Arial", Font.PLAIN, 14));
            radio.setBackground(Color.WHITE);
            radio.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            final int index = i;
            radio.addActionListener(e -> userAnswers.put(currentQuestion, List.of(index)));

            group.add(radio);
            buttons.add(radio);
            optionsPanel.add(radio);
        }
    }

    private void loadMMCQOptions(QuizQuestion q) {
        JLabel hint = new JLabel("(Select ALL correct answers)");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        optionsPanel.add(hint);
        optionsPanel.add(Box.createVerticalStrut(10));

        List<JCheckBox> checkboxes = new ArrayList<>();

        for (int i = 0; i < q.getOptions().size(); i++) {
            JCheckBox checkbox = new JCheckBox(q.getOptions().get(i));
            checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
            checkbox.setBackground(Color.WHITE);
            checkbox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            checkbox.addActionListener(e -> {
                List<Integer> selected = new ArrayList<>();
                for (int j = 0; j < checkboxes.size(); j++) {
                    if (checkboxes.get(j).isSelected()) {
                        selected.add(j);
                    }
                }
                userAnswers.put(currentQuestion, selected);
            });

            checkboxes.add(checkbox);
            optionsPanel.add(checkbox);
        }
    }

    private void loadNumericalInput(QuizQuestion q) {
        JLabel hint = new JLabel("Enter a number between 0-9:");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(5, 0, 9, 1));
        spinner.setFont(new Font("Arial", Font.BOLD, 24));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);

        spinner.addChangeListener(e -> {
            userAnswers.put(currentQuestion, List.of((Integer) spinner.getValue()));
        });

        optionsPanel.add(hint);
        optionsPanel.add(Box.createVerticalStrut(10));
        optionsPanel.add(spinner);

        userAnswers.put(currentQuestion, List.of(5)); // Default value
    }

    private void loadTrueFalseOptions(QuizQuestion q) {
        ButtonGroup group = new ButtonGroup();

        JRadioButton trueBtn = new JRadioButton("True");
        JRadioButton falseBtn = new JRadioButton("False");

        trueBtn.setFont(new Font("Arial", Font.BOLD, 16));
        falseBtn.setFont(new Font("Arial", Font.BOLD, 16));
        trueBtn.setBackground(Color.WHITE);
        falseBtn.setBackground(Color.WHITE);
        trueBtn.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        falseBtn.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        trueBtn.addActionListener(e -> userAnswers.put(currentQuestion, List.of(0)));
        falseBtn.addActionListener(e -> userAnswers.put(currentQuestion, List.of(1)));

        group.add(trueBtn);
        group.add(falseBtn);

        optionsPanel.add(trueBtn);
        optionsPanel.add(falseBtn);
    }

    private void nextQuestion() {
        if (!userAnswers.containsKey(currentQuestion)) {
            JOptionPane.showMessageDialog(this, "Please answer the current question!",
                    "Answer Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SoundManager.getInstance().playClick();
        currentQuestion++;
        loadQuestion();
    }

    private void submitQuiz() {
        quizTimer.stop();

        // Calculate score
        int totalPoints = 0;
        int earnedPoints = 0;

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            totalPoints += q.getPoints();

            List<Integer> userAns = userAnswers.getOrDefault(i, new ArrayList<>());
            if (q.isCorrect(userAns)) {
                earnedPoints += q.getPoints();
            }
        }

        score = (int) ((earnedPoints * 100.0) / totalPoints);

        SoundManager.getInstance().playSuccess();
        showResults();
        dispose();
    }

    private void showResults() {
        String message = String.format("""
            Quiz Completed!
            
            Your Score: %d%%
            Questions: %d
            Time Taken: %s
            
            %s
            """,
                score,
                questions.size(),
                formatTime(questions.size() * 60 - timeRemaining),
                score >= 80 ? "Excellent work!" : score >= 60 ? "Good job!" : "Keep practicing!"
        );

        JOptionPane.showMessageDialog(getParent(), message, "Quiz Results",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startTimer() {
        quizTimer = new Timer(1000, e -> {
            timeRemaining--;
            timerLabel.setText("Time: " + formatTime(timeRemaining));

            if (timeRemaining <= 0) {
                JOptionPane.showMessageDialog(this, "Time's up!", "Quiz Timeout",
                        JOptionPane.WARNING_MESSAGE);
                submitQuiz();
            } else if (timeRemaining <= 60) {
                timerLabel.setForeground(Color.RED);
            }
        });
        quizTimer.start();
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public int getScore() {
        return score;
    }
}
