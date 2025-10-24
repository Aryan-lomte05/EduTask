package com.edutask.service;

import com.edutask.model.QuizQuestion;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class QuizService {
    private List<QuizQuestion> questionBank;
    private static final String QUIZ_FILE = "resources/quiz-bank.json";

    public QuizService() {
        loadQuestions();
    }

    private void loadQuestions() {
        try {
            if (!Files.exists(Paths.get(QUIZ_FILE))) {
                createDefaultQuizBank();
            }
            String json = Files.readString(Paths.get(QUIZ_FILE));
            Gson gson = new Gson();
            QuizQuestion[] array = gson.fromJson(json, QuizQuestion[].class);
            questionBank = array == null ? new ArrayList<>() : Arrays.asList(array);
        } catch (Exception e) {
            System.err.println("Error loading quiz bank: " + e.getMessage());
            questionBank = new ArrayList<>();
        }
    }

    public List<QuizQuestion> getQuestions(String subject, String topic, int count) {
        List<QuizQuestion> filtered = questionBank.stream()
                .filter(q -> q.getSubject().equalsIgnoreCase(subject))
                .filter(q -> q.getTopic().equalsIgnoreCase(topic))
                .collect(Collectors.toList());

        Collections.shuffle(filtered);
        return filtered.stream().limit(count).collect(Collectors.toList());
    }

    private void createDefaultQuizBank() throws IOException {
        List<QuizQuestion> questions = Arrays.asList(
                new QuizQuestion("q1", "DSA", "Trees", "MCQ",
                        "What is the time complexity of searching in a balanced BST?",
                        new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"},
                        "O(log n)", "Binary search halves the search space each time.", 2),

                new QuizQuestion("q2", "DSA", "Trees", "TRUE_FALSE",
                        "In a complete binary tree, all levels are fully filled except possibly the last.",
                        new String[]{"True", "False"},
                        "True", "Definition of complete binary tree.", 1),

                new QuizQuestion("q3", "DBMS", "Normalization", "MCQ",
                        "Which normal form eliminates transitive dependencies?",
                        new String[]{"1NF", "2NF", "3NF", "BCNF"},
                        "3NF", "Third Normal Form removes transitive dependencies.", 2),

                new QuizQuestion("q4", "DBMS", "Normalization", "MCQ",
                        "A table is in 1NF if:",
                        new String[]{"All attributes are atomic", "No partial dependencies",
                                "No transitive dependencies", "All of the above"},
                        "All attributes are atomic", "1NF requires atomic values.", 1),

                new QuizQuestion("q5", "Java", "OOP", "MCQ",
                        "Which keyword is used to implement inheritance in Java?",
                        new String[]{"implements", "extends", "inherits", "super"},
                        "extends", "extends is used for class inheritance.", 1),

                new QuizQuestion("q6", "Java", "OOP", "TRUE_FALSE",
                        "Java supports multiple inheritance through classes.",
                        new String[]{"True", "False"},
                        "False", "Java does not support multiple class inheritance.", 1),

                new QuizQuestion("q7", "DSA", "Graphs", "MCQ",
                        "Which algorithm finds the shortest path in a weighted graph?",
                        new String[]{"BFS", "DFS", "Dijkstra's", "Prim's"},
                        "Dijkstra's", "Dijkstra's algorithm finds shortest paths.", 2),

                new QuizQuestion("q8", "DSA", "Sorting", "MCQ",
                        "Best case time complexity of QuickSort is:",
                        new String[]{"O(n)", "O(n log n)", "O(n²)", "O(log n)"},
                        "O(n log n)", "With good pivot selection, QuickSort is O(n log n).", 2)
        );

        Files.createDirectories(Paths.get("resources"));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(Paths.get(QUIZ_FILE), gson.toJson(questions));
    }
}
