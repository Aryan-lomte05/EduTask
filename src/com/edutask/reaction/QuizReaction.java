package com.edutask.reaction;

import com.edutask.model.*;
import com.edutask.service.QuizService;
import com.edutask.ui.QuizDialog;
import com.edutask.audio.SoundManager;
import javax.swing.*;

public class QuizReaction implements Reaction {
    private QuizService quizService;

    public QuizReaction(QuizService quizService) {
        this.quizService = quizService;
    }

    @Override
    public void execute(Task task) {  // ← Changed from trigger() to execute()
        // Only trigger quiz for Study tasks
        if (!(task instanceof StudyTask)) {
            return;
        }

        StudyTask studyTask = (StudyTask) task;
        String subject = studyTask.getSubject();
        String topic = studyTask.getTopic();

        // Show confirmation dialog
        int choice = JOptionPane.showConfirmDialog(
                null,
                String.format("""
                Great job completing "%s"!
                
                Would you like to take a quiz on:
                Subject: %s
                Topic: %s
                
                This will help reinforce your learning!
                """, task.getTitle(), subject, topic),
                "Quiz Challenge",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            SoundManager.getInstance().playAdd();

            // Show quiz configuration
            Object[] options = {"10 Questions", "15 Questions", "20 Questions"};
            int numQuestions = JOptionPane.showOptionDialog(
                    null,
                    "How many questions would you like?",
                    "Quiz Setup",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            int questionCount = switch (numQuestions) {
                case 0 -> 10;
                case 2 -> 20;
                default -> 15;
            };

            // Launch quiz
            SwingUtilities.invokeLater(() -> {
                try {
                    QuizDialog dialog = new QuizDialog(null, subject, topic, questionCount);
                    dialog.setVisible(true);

                    int score = dialog.getScore();
                    showResults(task.getTitle(), score, questionCount);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Quiz could not be loaded. Please try again.",
                            "Quiz Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void showResults(String taskTitle, int score, int questions) {
        String message;
        String title;

        if (score >= 80) {
            title = "Excellent!";
            message = String.format("Outstanding! You scored %d%% on %d questions for '%s'",
                    score, questions, taskTitle);
            SoundManager.getInstance().playSuccess();
        } else if (score >= 60) {
            title = "Good Job!";
            message = String.format("Well done! You scored %d%% on %d questions for '%s'",
                    score, questions, taskTitle);
            SoundManager.getInstance().playComplete();
        } else {
            title = "Keep Practicing!";
            message = String.format("You scored %d%% on %d questions for '%s'. Review and try again!",
                    score, questions, taskTitle);
        }

        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
