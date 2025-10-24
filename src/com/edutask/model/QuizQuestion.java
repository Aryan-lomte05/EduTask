package com.edutask.model;

import java.util.*;

public class QuizQuestion {
    public enum QuestionType {
        MCQ, MMCQ, NUMERICAL, TRUE_FALSE
    }

    private String question;
    private QuestionType type;
    private List<String> options;
    private List<Integer> correctAnswers;
    private String explanation;
    private int points;

    public QuizQuestion(String question, QuestionType type, List<String> options,
                        List<Integer> correctAnswers, String explanation, int points) {
        this.question = question;
        this.type = type;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswers = correctAnswers;
        this.explanation = explanation;
        this.points = points;
    }

    // Factory methods
    public static QuizQuestion createMCQ(String question, List<String> options, int correct) {
        return new QuizQuestion(question, QuestionType.MCQ, options, List.of(correct), "", 1);
    }

    public static QuizQuestion createMMCQ(String question, List<String> options, List<Integer> correct) {
        return new QuizQuestion(question, QuestionType.MMCQ, options, correct, "", 2);
    }

    public static QuizQuestion createNumerical(String question, int answer) {
        return new QuizQuestion(question, QuestionType.NUMERICAL, new ArrayList<>(),
                List.of(answer), "", 1);
    }

    public static QuizQuestion createTrueFalse(String question, boolean answer) {
        return new QuizQuestion(question, QuestionType.TRUE_FALSE,
                List.of("True", "False"), List.of(answer ? 0 : 1), "", 1);
    }

    // Getters
    public String getQuestion() { return question; }
    public QuestionType getType() { return type; }
    public List<String> getOptions() { return options; }
    public List<Integer> getCorrectAnswers() { return correctAnswers; }
    public String getExplanation() { return explanation; }
    public int getPoints() { return points; }

    public boolean isCorrect(List<Integer> userAnswers) {
        return new HashSet<>(userAnswers).equals(new HashSet<>(correctAnswers));
    }
}
