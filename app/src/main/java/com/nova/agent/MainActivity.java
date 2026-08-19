package com.nova.agent;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import android.app.Activity;

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

public class MainActivity extends Activity {

    private static final String MODEL = "gemini-3.6-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent";

    private EditText input;
    private TextView responseView;
    private Button executeButton;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUI();
    }

    private void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("NOVA");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);

        TextView subtitle = new TextView(this);
        subtitle.setText("Lightweight Android Agent • v0.2");
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.LTGRAY);

        TextView status = new TextView(this);
        status.setText("NOVA online • Gemini brain");
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 24, 0, 24);

        input = new EditText(this);
        input.setHint("Tell NOVA what to do...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setSingleLine(false);

        executeButton = new Button(this);
        executeButton.setText("EXECUTE");

        responseView = new TextView(this);
        responseView.setText("NOVA:\nReady.");
        responseView.setTextSize(16);
        responseView.setTextColor(Color.WHITE);
        responseView.setPadding(0, 30, 0, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(responseView);

        root.addView(title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(subtitle,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(status,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(input,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        150));

        root.addView(executeButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0, 1));

        root.setBackgroundColor(Color.rgb(12, 12, 18));

        setContentView(root);

        executeButton.setOnClickListener(v -> {
            String command = input.getText().toString().trim();

            if (command.isEmpty()) {
                responseView.setText("NOVA:\nTell me what you want me to do.");
                return;
            }

            askGemini(command);
        });
    }

    private void askGemini(String command) {

        String apiKey = BuildConfig.GEMINI_API_KEY;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            responseView.setText(
                    "NOVA:\nGemini API key is not configured."
            );
            return;
        }

        executeButton.setEnabled(false);
        responseView.setText("NOVA:\n🧠 Thinking...");

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
                        "action, target, response. " +
                        "Examples: " +
                        "{\"action\":\"open_app\",\"target\":\"chrome\"," +
                        "\"response\":\"Opening Chrome.\"} " +
                        "or " +
                        "{\"action\":\"response\",\"target\":\"\"," +
                        "\"response\":\"Hello! I am NOVA.\"}";

                JSONObject request = new JSONObject();

                JSONArray contents = new JSONArray();

                JSONObject content = new JSONObject();
                content.put("role", "user");

                JSONArray parts = new JSONArray();

                JSONObject part = new JSONObject();
                part.put("text",
                        systemInstruction +
                        "\n\nUSER COMMAND:\n" + command);

                parts.put(part);
                content.put("parts", parts);
                contents.put(content);

                request.put("contents", contents);

                JSONObject generationConfig = new JSONObject();
                generationConfig.put(
                        "responseMimeType",
                        "application/json"
                );

                request.put(
                        "generationConfig",
                        generationConfig
                );

                URL url = new URL(API_URL);

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "x-goog-api-key",
                        apiKey
                );

                byte[] body =
                        request.toString()
                                .getBytes(StandardCharsets.UTF_8);

                OutputStream output =
                        connection.getOutputStream();

                output.write(body);
                output.flush();
                output.close();

                int status = connection.getResponseCode();

                InputStream stream;

                if (status >= 200 && status < 300) {
                    stream = connection.getInputStream();
                } else {
                    stream = connection.getErrorStream();
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(stream)
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                if (status < 200 || status >= 300) {
                    showResult(
                            "Gemini API error: HTTP " + status
                    );
                    return;
                }

                JSONObject response =
                        new JSONObject(result.toString());

                String text =
                        response
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                handleGeminiAction(text);

            } catch (Exception e) {

                showResult(
                        "Connection error:\n" +
                        e.getMessage()
                );
            }
        });
    }

    private void handleGeminiAction(String jsonText) {

        try {

            JSONObject action =
                    new JSONObject(jsonText.trim());

            String actionName =
                    action.optString("action", "");

            String target =
                    action.optString("target", "");

            String response =
                    action.optString("response",
                            "Done.");

            if ("response".equals(actionName)) {

                showResult("NOVA:\n" + response);
                return;
            }

            if ("open_app".equals(actionName)) {

                if ("chrome".equalsIgnoreCase(target)) {

                    openPackage(
                            "com.android.chrome",
                            "Chrome",
                            response
                    );

                    return;
                }

                if ("youtube".equalsIgnoreCase(target)) {

                    openPackage(
                            "com.google.android.youtube",
                            "YouTube",
                            response
                    );

                    return;
                }
            }

            showResult(
                    "NOVA:\nI can't perform that action yet."
            );

        } catch (Exception e) {

            showResult(
                    "NOVA:\nI couldn't understand Gemini's response."
            );
        }
    }

    private void openPackage(
            String packageName,
            String appName,
            String response
    ) {

        runOnUiThread(() -> {

            executeButton.setEnabled(true);

            PackageManager pm = getPackageManager();

            Intent launch =
                    pm.getLaunchIntentForPackage(packageName);

            if (launch == null) {

                responseView.setText(
                        "NOVA:\n" +
                        appName +
                        " is not installed."
                );

                return;
            }

            responseView.setText(
                    "NOVA:\n" + response
            );

            startActivity(launch);
        });
    }

    private void showResult(String message) {

        runOnUiThread(() -> {

            executeButton.setEnabled(true);
            responseView.setText(message);
        });
    }

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}
