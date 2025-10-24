package com.edutask.ai;

import com.edutask.model.QuizQuestion;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class AIQuizGenerator {
    private static final String GEMINI_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
    private String apiKey = "AIzaSyBNDpn7m04jLj_JeOrld3zgez2b4qvO22k"; // Replace with your key
    private Gson gson = new Gson();

    // Maximum batch size per API call to avoid timeout
    private static final int BATCH_SIZE = 5;

    public List<QuizQuestion> generateQuiz(String subject, String topic, int numQuestions) {
        List<QuizQuestion> questions = new ArrayList<>();
        System.out.println("🤖 Generating quiz with AI...");
        System.out.println("📚 " + subject + " → " + topic);

        if (apiKey.isEmpty()) {
            System.out.println("⚠️ No API key set. Falling back to generic questions.");
            return generateSmartFallback(subject, topic, numQuestions);
        }

        try {
            for (int i = 0; i < numQuestions; i += BATCH_SIZE) {
                int batchCount = Math.min(BATCH_SIZE, numQuestions - i);
                String prompt = buildDynamicPrompt(subject, topic, batchCount);
                String response = callGeminiAPI(prompt);
                List<QuizQuestion> batchQuestions = parseCompleteQuiz(response, batchCount);

                if (batchQuestions.isEmpty()) {
                    // Fallback for this batch
                    batchQuestions = generateSmartFallback(subject, topic, batchCount);
                }
                questions.addAll(batchQuestions);
            }
        } catch (Exception e) {
            System.err.println("❌ AI Error: " + e.getMessage());
            return generateSmartFallback(subject, topic, numQuestions);
        }

        Collections.shuffle(questions);
        System.out.println("✅ Quiz ready: " + questions.size() + " questions");
        return questions;
    }

    private String buildDynamicPrompt(String subject, String topic, int count) {
        return String.format("""
                Generate %d quiz questions about "%s" in %s.
                Mix of question types:
                - Multiple Choice (MCQ) with 4 options
                - True/False statements

                Format each question as:
                [MCQ]
                Question: ...
                A) ...
                B) ...
                C) ...
                D) ...
                Answer: ...

                [TRUE_FALSE]
                Statement: ...
                Answer: TRUE/FALSE

                Generate %d educational questions now.
                """, count, topic, subject, count);
    }

    @SuppressWarnings("deprecation")
    private String callGeminiAPI(String prompt) throws IOException {
        URL url = new URL(GEMINI_API + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000); // 60 seconds
        conn.setReadTimeout(60000);

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

        try (OutputStream os = conn.getOutputStream()) {
            os.write(request.toString().getBytes());
        }

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

    // Parsing logic (same as before)
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
            if (line.startsWith("Question:")) question = line.substring(9).trim();
            else if (line.matches("^[A-D]\\).*")) options.add(line.substring(3).trim());
            else if (line.startsWith("Answer:")) correct = line.substring(7).trim().toUpperCase().charAt(0) - 'A';
        }

        if (!question.isEmpty() && options.size() == 4)
            return QuizQuestion.createMCQ(question, options, correct);
        return null;
    }

    private QuizQuestion parseTrueFalse(String block) {
        String[] lines = block.split("\n");
        String statement = "";
        boolean answer = true;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Statement:")) statement = line.substring(10).trim();
            else if (line.startsWith("Answer:")) answer = line.substring(7).trim().equalsIgnoreCase("TRUE");
        }

        if (!statement.isEmpty()) return QuizQuestion.createTrueFalse(statement, answer);
        return null;
    }

    private List<QuizQuestion> generateSmartFallback(String subject, String topic, int count) {
        List<QuizQuestion> questions = new ArrayList<>();
        // Add subject-specific fallback here (Math Surds example)
        if (subject.toLowerCase().contains("math") && topic.toLowerCase().contains("surd")) {
            questions.add(QuizQuestion.createMCQ("Which of the following is a surd?", List.of("√16","√7","√9","√25"), 1));
            questions.add(QuizQuestion.createMCQ("Simplify √48:", List.of("4√3","6√2","2√12","3√16"),0));
            questions.add(QuizQuestion.createTrueFalse("A surd is an irrational number", true));
            questions.add(QuizQuestion.createMCQ("Rationalize 1/√3:", List.of("√3/3","1/3","3/√3","√3"),0));
            questions.add(QuizQuestion.createTrueFalse("√a × √b = √(ab) for all positive a and b", true));
        }

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
