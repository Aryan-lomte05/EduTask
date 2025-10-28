package com.edutask.ai;

import com.edutask.model.QuizQuestion;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class AIQuizGenerator {
    private static final String GEMINI_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private String apiKey = "AIzaSyAGaQ0wjdOVZt8EDNHouY7Gs2t0N9w9BOE";
    private Gson gson = new Gson();
    private static final int BATCH_SIZE = 5;
    private static final int MAX_RETRIES = 3;

    public List<QuizQuestion> generateQuiz(String subject, String topic, int numQuestions) {
        List<QuizQuestion> questions = new ArrayList<>();
        System.out.println("🤖 Generating quiz with AI...");
        System.out.println("📚 " + subject + " → " + topic);

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("⚠️ API key missing! Using fallback.");
            return generateSmartFallback(subject, topic, numQuestions);
        }

        try {
            for (int i = 0; i < numQuestions; i += BATCH_SIZE) {
                int batchCount = Math.min(BATCH_SIZE, numQuestions - i);
                List<QuizQuestion> batch = generateBatchWithRetry(subject, topic, batchCount, i / BATCH_SIZE + 1);
                questions.addAll(batch);
            }
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
            return generateSmartFallback(subject, topic, numQuestions);
        }

        Collections.shuffle(questions);
        System.out.println("✅ Quiz ready: " + questions.size() + " questions");
        return questions;
    }

    private List<QuizQuestion> generateBatchWithRetry(String subject, String topic, int count, int batchNum) {
        System.out.println("\n=== BATCH " + batchNum + " (" + count + " questions) ===");

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                String prompt = buildDynamicPrompt(subject, topic, count);
                String response = callGeminiAPI(prompt);

                if (retry == 0) {
                    System.out.println("📥 RAW RESPONSE:\n" + response.substring(0, Math.min(500, response.length())) + "...");
                }

                response = cleanResponse(response);
                List<QuizQuestion> batch = parseCompleteQuiz(response, count);

                if (!batch.isEmpty()) {
                    System.out.println("✅ Parsed " + batch.size() + "/" + count + " questions");
                    return batch;
                }

                System.err.println("⚠️ Retry " + (retry + 1) + "/" + MAX_RETRIES + " - parsing failed");
                Thread.sleep(1000); // Wait before retry

            } catch (IOException e) {
                System.err.println("❌ Retry " + (retry + 1) + "/" + MAX_RETRIES + " - API error: " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ie) { }
            } catch (Exception e) {
                System.err.println("❌ Retry " + (retry + 1) + "/" + MAX_RETRIES + " - error: " + e.getMessage());
            }
        }

        System.err.println("⚠️ All retries failed → Using smart fallback");
        return generateSmartFallback(subject, topic, count);
    }

    private String buildDynamicPrompt(String subject, String topic, int count) {
        return String.format("""
            You are a precise quiz generator. Generate EXACTLY %d questions on "%s" in "%s".
            Output ONLY in this format. NO extra text, NO markdown, NO explanations.

            RULES:
            1. Use |||MCQ||| or |||TF||| to start each question
            2. End each with |||END|||
            3. For math: use ∅ (empty set), ∪ (union), ∩ (intersection), ⊆ (subset)
            4. MCQ: exactly 4 options A-D, Answer: A/B/C/D
            5. TF: Answer: TRUE or FALSE
            6. NO extra symbols (✓, ✗, *, bullets)

            EXAMPLE MCQ:
            |||MCQ|||
            Question: What is ∅ in set theory?
            A) Empty set
            B) Full set
            C) Universal set
            D) Complement set
            Answer: A
            |||END|||

            EXAMPLE TF:
            |||TF|||
            Statement: ∅ ⊆ A for any set A
            Answer: TRUE
            |||END|||

            Generate %d questions now:
            """, count, topic, subject, count);
    }

    private String callGeminiAPI(String prompt) throws IOException {
        URL url;
        try {
            url = new URI(GEMINI_API + "?key=" + apiKey).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid API URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);

        JsonObject request = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject userMsg = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        userMsg.add("parts", parts);
        userMsg.addProperty("role", "user");
        contents.add(userMsg);
        request.add("contents", contents);

        JsonArray safety = new JsonArray();
        JsonObject s = new JsonObject();
        s.addProperty("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        s.addProperty("threshold", "BLOCK_NONE");
        safety.add(s);
        request.add("safetySettings", safety);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(gson.toJson(request).getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            String err = readStream(conn.getErrorStream());
            throw new IOException("HTTP " + code + ": " + err);
        }

        String response = readStream(conn.getInputStream());
        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonArray candidates = json.getAsJsonArray("candidates");
        if (candidates == null || candidates.size() == 0) {
            throw new IOException("No candidates in response");
        }
        return candidates.get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    private String cleanResponse(String response) {
        if (response == null) return "";

        // Remove markdown code blocks

                response = response.replaceAll("```", "");

        // Remove extra symbols that break parsing
        response = response.replaceAll("[✓✗❌✅*•]", "");

        // Normalize delimiters (handle extra spaces)
        response = response.replaceAll("\\|\\|\\|\\s*MCQ\\s*\\|\\|\\|", "|||MCQ|||");
        response = response.replaceAll("\\|\\|\\|\\s*TF\\s*\\|\\|\\|", "|||TF|||");
        response = response.replaceAll("\\|\\|\\|\\s*END\\s*\\|\\|\\|", "|||END|||");

        // Remove leading/trailing whitespace
        return response.trim();
    }

    private List<QuizQuestion> parseCompleteQuiz(String response, int expected) {
        List<QuizQuestion> questions = new ArrayList<>();
        if (response == null || response.trim().isEmpty()) return questions;

        // Split by question markers
        String[] blocks = response.split("(?=\\|\\|\\|(?:MCQ|TF)\\|\\|\\|)");

        for (String block : blocks) {
            if (block == null || block.trim().isEmpty()) continue;

            try {
                block = block.trim();
                if (block.startsWith("|||MCQ|||")) {
                    QuizQuestion q = parseMCQ(block);
                    if (q != null) {
                        questions.add(q);
                        System.out.println("  ✓ MCQ: " + truncate(q.getQuestion(), 60));
                    }
                } else if (block.startsWith("|||TF|||")) {
                    QuizQuestion q = parseTrueFalse(block);
                    if (q != null) {
                        questions.add(q);
                        System.out.println("  ✓ TF: " + truncate(q.getQuestion(), 60));
                    }
                }
            } catch (Exception e) {
                System.err.println("  ✗ Parse error: " + e.getMessage());
            }
        }

        return questions;
    }

    private QuizQuestion parseMCQ(String block) {
        // Remove delimiters
        block = block.replace("|||MCQ|||", "").replace("|||END|||", "").trim();

        String question = "";
        List<String> options = new ArrayList<>();
        String answer = "";

        // Use regex for robust extraction
        Pattern questionPattern = Pattern.compile("Question:\\s*(.+?)(?=\\s*A\\))", Pattern.DOTALL);
        Matcher qMatcher = questionPattern.matcher(block);
        if (qMatcher.find()) {
            question = unescape(qMatcher.group(1).trim());
        }

        // Extract options A-D
        Pattern optionPattern = Pattern.compile("([A-D])\\)\\s*(.+?)(?=[A-D]\\)|Answer:|$)", Pattern.DOTALL);
        Matcher oMatcher = optionPattern.matcher(block);
        while (oMatcher.find() && options.size() < 4) {
            options.add(unescape(oMatcher.group(2).trim()));
        }

        // Extract answer
        Pattern answerPattern = Pattern.compile("Answer:\\s*([A-D])", Pattern.CASE_INSENSITIVE);
        Matcher aMatcher = answerPattern.matcher(block);
        if (aMatcher.find()) {
            answer = aMatcher.group(1).toUpperCase();
        }

        if (!question.isEmpty() && options.size() == 4 && answer.matches("[A-D]")) {
            int correct = answer.charAt(0) - 'A';
            return QuizQuestion.createMCQ(question, options, correct);
        }

        return null;
    }

    private QuizQuestion parseTrueFalse(String block) {
        block = block.replace("|||TF|||", "").replace("|||END|||", "").trim();

        String statement = "";
        String answer = "";

        Pattern statementPattern = Pattern.compile("Statement:\\s*(.+?)(?=Answer:)", Pattern.DOTALL);
        Matcher sMatcher = statementPattern.matcher(block);
        if (sMatcher.find()) {
            statement = unescape(sMatcher.group(1).trim());
        }

        Pattern answerPattern = Pattern.compile("Answer:\\s*(TRUE|FALSE)", Pattern.CASE_INSENSITIVE);
        Matcher aMatcher = answerPattern.matcher(block);
        if (aMatcher.find()) {
            answer = aMatcher.group(1).toUpperCase();
        }

        if (!statement.isEmpty() && (answer.equals("TRUE") || answer.equals("FALSE"))) {
            return QuizQuestion.createTrueFalse(statement, answer.equals("TRUE"));
        }

        return null;
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    private List<QuizQuestion> generateSmartFallback(String subject, String topic, int count) {
        List<QuizQuestion> questions = new ArrayList<>();
        String subjectLower = subject.toLowerCase();
        String topicLower = topic.toLowerCase();

        // MATH - SURDS
        if (subjectLower.contains("math") && topicLower.contains("surd")) {
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
                    "A surd is an irrational number that cannot be simplified to remove the square root",
                    true
            ));
            questions.add(QuizQuestion.createMCQ(
                    "Rationalize the denominator: 1/√3",
                    List.of("√3/3", "1/3", "3/√3", "√3"),
                    0
            ));
            questions.add(QuizQuestion.createTrueFalse(
                    "√a × √b = √(ab) for all positive real numbers a and b",
                    true
            ));
        }

        // MATH - SET THEORY
        else if (subjectLower.contains("math") && (topicLower.contains("set") || topicLower.contains("∅"))) {
            questions.add(QuizQuestion.createTrueFalse(
                    "The empty set ∅ is a subset of every set",
                    true
            ));
            questions.add(QuizQuestion.createMCQ(
                    "What is A ∪ ∅ for any set A?",
                    List.of("∅", "A", "Universal set", "A'"),
                    1
            ));
            questions.add(QuizQuestion.createMCQ(
                    "What is A ∩ ∅ for any set A?",
                    List.of("∅", "A", "Universal set", "A'"),
                    0
            ));
        }

        // DSA - TREES
        else if (subjectLower.contains("dsa") || topicLower.contains("tree")) {
            questions.add(QuizQuestion.createMCQ(
                    "What is the time complexity of searching in a balanced BST?",
                    List.of("O(1)", "O(log n)", "O(n)", "O(n²)"),
                    1
            ));
            questions.add(QuizQuestion.createTrueFalse(
                    "A binary tree has at most 2 children per node",
                    true
            ));
        }

        // GENERIC FALLBACK
        while (questions.size() < count) {
            questions.add(QuizQuestion.createMCQ(
                    "What is a key concept when studying " + topic + "?",
                    List.of("Understanding theory", "Practicing problems", "Reviewing regularly", "All of the above"),
                    3
            ));
        }

        return questions.subList(0, Math.min(count, questions.size()));
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }
}
