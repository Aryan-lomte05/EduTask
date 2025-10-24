package com.edutask.reaction;

import com.edutask.model.*;
import com.edutask.service.QuizService;
import com.edutask.ui.QuizDialog;
import javax.swing.SwingUtilities;

public class QuizReaction implements Reaction {
    private QuizService quizService;

    public QuizReaction(QuizService quizService) {
        this.quizService = quizService;
    }

    @Override
    public void execute(Task task) {
        if (task instanceof StudyTask st) {
            SwingUtilities.invokeLater(() -> {
                QuizDialog dialog = new QuizDialog(null, st, quizService);
                dialog.setVisible(true);
            });
        }
    }
}
