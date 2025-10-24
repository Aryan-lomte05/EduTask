package com.edutask.service;

import com.edutask.model.*;
import com.edutask.reaction.*;
import com.edutask.events.*;
import java.util.*;

public class ReactionService implements TaskEventListener<TaskCompletedEvent> {
    private Map<Category, Reaction> reactions;
    private QuizService quizService;

    public ReactionService(QuizService quizService) {
        this.quizService = quizService;
        initializeReactions();
    }

    private void initializeReactions() {
        reactions = new HashMap<>();
        reactions.put(Category.STUDY, new QuizReaction(quizService));
        reactions.put(Category.PERSONAL, new JournalReaction());
    }

    @Override
    public void onEvent(TaskCompletedEvent event) {
        Task task = event.getTask();
        Reaction reaction = reactions.get(task.getCategory());
        if (reaction != null) {
            reaction.execute(task);
        }
    }
}
