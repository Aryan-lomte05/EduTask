package com.edutask.ai;

import com.edutask.model.QuizQuestion;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class AIQuizGenerator {
    // Google Gemini API - FREE, 60 requests/minute
    // Get key from: https://makersuite.google.com/app/apikey
    private static final String GEMINI_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private String apiKey = "AIzaSyAGaQ0wjdOVZt8EDNHouY7Gs2t0N9w9BOE"; // GET YOUR FREE KEY: makersuite.google.com/app/apikey
    private Gson gson = new Gson();

    public List<QuizQuestion> generateQuiz(String subject, String topic, int numQuestions) {
        List<QuizQuestion> questions = new ArrayList<>();

        System.out.println("🤖 Generating quiz with AI...");
        System.out.println("📚 " + subject + " → " + topic);

        if (apiKey.isEmpty()) {
            System.out.println("⚠️ No API key set. Get free key from: makersuite.google.com/app/apikey");
            return generateSmartFallback(subject, topic, numQuestions);
        }

        try {
            // Generate all questions in one API call (faster!)
            String prompt = buildCompleteQuizPrompt(subject, topic, numQuestions);
            String response = callGeminiAPI(prompt);

            System.out.println("✅ AI Response received");
            questions = parseCompleteQuiz(response, numQuestions);

            if (questions.size() < numQuestions) {
                int needed = numQuestions - questions.size();
                System.out.println("📝 Adding " + needed + " fallback questions");
                questions.addAll(generateSmartFallback(subject, topic, needed));
            }

        } catch (Exception e) {
            System.err.println("❌ AI Error: " + e.getMessage());
            return generateSmartFallback(subject, topic, numQuestions);
        }

        Collections.shuffle(questions);
        System.out.println("✅ Quiz ready: " + questions.size() + " questions");
        return questions;
    }

    private String buildCompleteQuizPrompt(String subject, String topic, int count) {
        return String.format("""
            Generate %d quiz questions about "%s" in %s.
            
            Mix of question types:
            - Multiple Choice (MCQ): 4 options, single answer
            - True/False: Simple statement
            
            Format EXACTLY like this:
            
            [MCQ]
            Question: What is the definition of %s?
            A) Option 1
            B) Option 2
            C) Option 3  
            D) Option 4
            Answer: C
            
            [TRUE_FALSE]
            Statement: %s is important in %s
            Answer: TRUE
            
            Generate challenging, educational questions now:
            """, count, topic, subject, topic, topic, subject);
    }

    @SuppressWarnings("deprecation")
    private String callGeminiAPI(String prompt) throws IOException {
        URL url = new URL(GEMINI_API + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        // Build Gemini request
        JsonObject request = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        request.add("contents", contents);

        // Send
        try (OutputStream os = conn.getOutputStream()) {
            os.write(request.toString().getBytes());
        }

        // Read response
        int code = conn.getResponseCode();
        if (code != 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) error.append(line);
                throw new IOException("Gemini API Error " + code + ": " + error);
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }

        // Parse Gemini response
        JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

        if (candidates != null && candidates.size() > 0) {
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject contentObj = candidate.getAsJsonObject("content");
            JsonArray partsArr = contentObj.getAsJsonArray("parts");

            if (partsArr != null && partsArr.size() > 0) {
                return partsArr.get(0).getAsJsonObject().get("text").getAsString();
            }
        }

        throw new IOException("Empty response from Gemini");
    }

    private List<QuizQuestion> parseCompleteQuiz(String response, int expectedCount) {
        List<QuizQuestion> questions = new ArrayList<>();
        String[] blocks = response.split("\\[(?=MCQ|TRUE_FALSE)");

        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;

            try {
                if (block.startsWith("MCQ]")) {
                    QuizQuestion q = parseMCQ(block);
                    if (q != null) questions.add(q);
                } else if (block.startsWith("TRUE_FALSE]")) {
                    QuizQuestion q = parseTrueFalse(block);
                    if (q != null) questions.add(q);
                }
            } catch (Exception e) {
                System.err.println("Parse error: " + e.getMessage());
            }
        }

        return questions;
    }

    private QuizQuestion parseMCQ(String block) {
        String[] lines = block.split("\n");
        String question = "";
        List<String> options = new ArrayList<>();
        int correct = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Question:")) {
                question = line.substring(9).trim();
            } else if (line.matches("^[A-D]\\).*")) {
                options.add(line.substring(3).trim());
            } else if (line.startsWith("Answer:")) {
                char ans = line.substring(7).trim().toUpperCase().charAt(0);
                correct = ans - 'A';
            }
        }

        if (!question.isEmpty() && options.size() == 4) {
            return QuizQuestion.createMCQ(question, options, correct);
        }
        return null;
    }

    private QuizQuestion parseTrueFalse(String block) {
        String[] lines = block.split("\n");
        String statement = "";
        boolean answer = true;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Statement:")) {
                statement = line.substring(10).trim();
            } else if (line.startsWith("Answer:")) {
                answer = line.substring(7).trim().equalsIgnoreCase("TRUE");
            }
        }

        if (!statement.isEmpty()) {
            return QuizQuestion.createTrueFalse(statement, answer);
        }
        return null;
    }

    // Smart fallback with your improved Maths/Surds questions
    private List<QuizQuestion> generateSmartFallback(String subject, String topic, int count) {
        List<QuizQuestion> questions = new ArrayList<>();

        // Math-specific questions
        if (subject.toLowerCase().contains("math") && topic.toLowerCase().contains("surd")) {
            questions.add(QuizQuestion.createMCQ(
                    "Which of the following is a surd?",
                    List.of("√16", "√7", "√9", "√25"),
                    1
            ));

            questions.add(QuizQuestion.createMCQ(
                    "Simplify √48:",
                    List.of("4√3", "6√2", "2√12", "3√16"),
                    0
            ));

            questions.add(QuizQuestion.createTrueFalse(
                    "A surd is an irrational number",
                    true
            ));

            questions.add(QuizQuestion.createMCQ(
                    "Rationalize 1/√3:",
                    List.of("√3/3", "1/3", "3/√3", "√3"),
                    0
            ));

            questions.add(QuizQuestion.createTrueFalse(
                    "√a × √b = √(ab) for all positive a and b",
                    true
            ));
        }

        // Generic questions to fill
        while (questions.size() < count) {
            questions.add(QuizQuestion.createMCQ(
                    "What is important when studying " + topic + "?",
                    List.of("Understanding concepts", "Practice problems", "Review regularly", "All of the above"),
                    3
            ));
        }

        return questions.subList(0, Math.min(count, questions.size()));
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }
}

