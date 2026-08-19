package com.nova.agent;

import android.app.Activity;
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

public class ChatActivity extends Activity {

    private LinearLayout messagesContainer;
    private ScrollView messagesScroll;
    private EditText input;
    private Button sendButton;
    private TextView thinkingIndicator;
    private final GeminiClient geminiClient = new GeminiClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
        renderHistory();
    }

    @Override
    protected void onDestroy() {
        geminiClient.shutdown();
        super.onDestroy();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(NovaUi.BG);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setPadding(32, 24, 32, 8);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = NovaUi.header(this, "Chat");
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        headerRow.addView(title, titleLp);

        Button clearButton = new Button(this);
        clearButton.setText("Clear");
        clearButton.setAllCaps(false);
        clearButton.setBackgroundColor(NovaUi.BG_CARD);
        clearButton.setTextColor(NovaUi.TEXT_SECONDARY);
        clearButton.setOnClickListener(v -> {
            NovaState.INSTANCE.clearChat();
            messagesContainer.removeAllViews();
        });
        headerRow.addView(clearButton);

        root.addView(headerRow);

        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(24, 8, 24, 8);

        messagesScroll = new ScrollView(this);
        messagesScroll.addView(messagesContainer);

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(messagesScroll, scrollLp);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setPadding(16, 8, 16, 16);

        input = new EditText(this);
        input.setHint("Message NOVA...");
        input.setTextColor(NovaUi.TEXT_PRIMARY);
        input.setHintTextColor(NovaUi.TEXT_SECONDARY);

        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        inputRow.addView(input, inputLp);

        sendButton = new Button(this);
        sendButton.setText("Send");
        sendButton.setAllCaps(false);
        sendButton.setBackgroundColor(NovaUi.ACCENT);
        sendButton.setTextColor(Color.BLACK);
        sendButton.setOnClickListener(v -> onSend());
        inputRow.addView(sendButton);

        root.addView(inputRow);
        root.addView(NovaUi.bottomNav(this, ChatActivity.class));

        setContentView(root);
    }

    private void renderHistory() {
        for (NovaState.Message m : NovaState.INSTANCE.chatHistory) {
            addBubble(m.text, m.fromUser);
        }
    }

    private void onSend() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;

        input.setText("");
        NovaState.INSTANCE.addUserMessage(text);
        addBubble(text, true);

        sendButton.setEnabled(false);
        thinkingIndicator = addBubble("Thinking...", false);

        String apiKey = BuildConfig.GEMINI_API_KEY;

        geminiClient.ask(apiKey, text, new GeminiClient.Callback() {
            @Override
            public void onAction(String action, String target, String response) {
                runOnUiThread(() -> {
                    removeThinkingIndicator();
                    NovaState.INSTANCE.addNovaMessage(response);
                    addBubble(response, false);
                    sendButton.setEnabled(true);

                    if ("open_app".equals(action)) {
                        launchTarget(target);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    removeThinkingIndicator();
                    NovaState.INSTANCE.addNovaMessage(message);
                    addBubble(message, false);
                    sendButton.setEnabled(true);
                });
            }
        });
    }

    private void launchTarget(String target) {
        String packageName = null;
        String appName = null;

        if ("chrome".equalsIgnoreCase(target)) {
            packageName = "com.android.chrome";
            appName = "Chrome";
        } else if ("youtube".equalsIgnoreCase(target)) {
            packageName = "com.google.android.youtube";
            appName = "YouTube";
        }

        if (packageName == null) return;

        PackageManager pm = getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage(packageName);

        if (launch == null) {
            addBubble(appName + " is not installed.", false);
            return;
        }

        startActivity(launch);
    }

    private void removeThinkingIndicator() {
        if (thinkingIndicator != null) {
            messagesContainer.removeView(thinkingIndicator);
            thinkingIndicator = null;
        }
    }

    private TextView addBubble(String text, boolean fromUser) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(NovaUi.TEXT_PRIMARY);
        bubble.setTextSize(15);
        bubble.setPadding(24, 16, 24, 16);
        bubble.setBackgroundColor(fromUser ? NovaUi.BUBBLE_USER : NovaUi.BUBBLE_NOVA);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        lp.gravity = fromUser ? Gravity.END : Gravity.START;
        bubble.setLayoutParams(lp);

        messagesContainer.addView(bubble);
        messagesScroll.post(() -> messagesScroll.fullScroll(ScrollView.FOCUS_DOWN));

        return bubble;
    }
}
