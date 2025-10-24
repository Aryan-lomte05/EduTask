package com.edutask.model;

public class QuizQuestion {
    private String id;
    private String subject;
    private String topic;
    private String type; // MCQ, TRUE_FALSE, FILL
    private String prompt;
    private String[] options; // For MCQ
    private String answerKey;
    private String explanation;
    private int difficulty; // 1-3

    public QuizQuestion(String id, String subject, String topic, String type,
                        String prompt, String[] options, String answerKey,
                        String explanation, int difficulty) {
        this.id = id;
        this.subject = subject;
        this.topic = topic;
        this.type = type;
        this.prompt = prompt;
        this.options = options;
        this.answerKey = answerKey;
        this.explanation = explanation;
        this.difficulty = difficulty;
    }

    // Getters
    public String getId() { return id; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public String getType() { return type; }
    public String getPrompt() { return prompt; }
    public String[] getOptions() { return options; }
    public String getAnswerKey() { return answerKey; }
    public String getExplanation() { return explanation; }
    public int getDifficulty() { return difficulty; }
}
