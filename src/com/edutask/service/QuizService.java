package com.edutask.service;

import com.edutask.model.QuizQuestion;
import com.edutask.ai.AIQuizGenerator;
import java.util.*;

public class QuizService {
    private AIQuizGenerator aiGenerator;

    public QuizService() {
        this.aiGenerator = new AIQuizGenerator();
    }

    /**
     * Get quiz questions for a subject/topic
     * @param subject The subject
     * @param topic The topic
     * @param count Number of questions (10-20)
     * @return List of quiz questions
     */
    public List<QuizQuestion> getQuizQuestions(String subject, String topic, int count) {
        return aiGenerator.generateQuiz(subject, topic, count);
    }

    /**
     * Get default quiz (15 questions)
     */
    public List<QuizQuestion> getQuizForTask(String subject, String topic) {
        return getQuizQuestions(subject, topic, 15);
    }

    /**
     * Set API key for AI generator
     */
    public void setApiKey(String apiKey) {
        aiGenerator.setApiKey(apiKey);
    }
}
