package com.nova.agent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiClient {

    public interface Callback {
        void onAction(String action, String target, String response);
        void onError(String message);
    }

    private static final String MODEL = "gemini-3.6-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void ask(String apiKey, String command, Callback callback) {

        NovaState.INSTANCE.model = MODEL;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            NovaState.INSTANCE.apiKeyConfigured = false;
            callback.onError("NOVA:\nGemini API key is not configured.");
            return;
        }

        NovaState.INSTANCE.apiKeyConfigured = true;

        executor.execute(() -> {
            try {

                String systemInstruction =
                        "You are NOVA, a lightweight Android agent. " +
                        "You are the reasoning brain of NOVA. " +
                        "You do NOT directly execute actions. " +
                        "You only return safe structured JSON. " +
                        "Supported actions are: response and open_app. " +
                        "open_app targets allowed are chrome and youtube. " +
                        "Never invent other actions. " +
                        "Never return shell commands. " +
                        "Never return arbitrary Android intents. " +
                        "Return ONLY valid JSON with exactly these fields: " +
                        "action, target, response.";

                JSONObject part = new JSONObject();
                part.put("text", systemInstruction + "\n\nUSER COMMAND:\n" + command);

                JSONArray parts = new JSONArray();
                parts.put(part);

                JSONObject content = new JSONObject();
                content.put("role", "user");
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject generationConfig = new JSONObject();
                generationConfig.put("responseMimeType", "application/json");

                JSONObject request = new JSONObject();
                request.put("contents", contents);
                request.put("generationConfig", generationConfig);

                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("x-goog-api-key", apiKey);

                byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream output = connection.getOutputStream();
                output.write(body);
                output.flush();
                output.close();

                int status = connection.getResponseCode();

                InputStream stream = (status >= 200 && status < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                connection.disconnect();

                if (status < 200 || status >= 300) {
                    NovaState.INSTANCE.connectionStatus = NovaState.ConnectionStatus.ERROR;
                    NovaState.INSTANCE.lastRequestStatus = "HTTP " + status;
                    callback.onError("NOVA:\nGemini API error: HTTP " + status);
                    return;
                }

                JSONObject response = new JSONObject(result.toString());

                String text = response
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                JSONObject action = new JSONObject(text.trim());

                String actionName = action.optString("action", "response");
                String target = action.optString("target", "");
                String responseText = action.optString("response", "Done.");

                NovaState.INSTANCE.connectionStatus = NovaState.ConnectionStatus.ONLINE;
                NovaState.INSTANCE.lastRequestStatus = "OK";

                callback.onAction(actionName, target, responseText);

            } catch (Exception e) {
                NovaState.INSTANCE.connectionStatus = NovaState.ConnectionStatus.ERROR;
                NovaState.INSTANCE.lastRequestStatus = "Error: " + e.getMessage();
                callback.onError("NOVA:\nConnection error:\n" + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
