package com.nova.agent;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(NovaUi.BG);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 24, 32, 16);

        content.addView(NovaUi.header(this, "Settings"));

        boolean keyConfigured = BuildConfig.GEMINI_API_KEY != null
                && !BuildConfig.GEMINI_API_KEY.trim().isEmpty();

        LinearLayout geminiCard = NovaUi.card(this);
        geminiCard.addView(NovaUi.label(this, "GEMINI CONFIGURATION"));
        geminiCard.addView(NovaUi.value(this,
                "Set via local.properties on the build machine."));
        geminiCard.addView(NovaUi.label(this, "API CONFIGURATION STATUS"));
        geminiCard.addView(NovaUi.value(this, keyConfigured ? "Configured" : "Not configured"));
        content.addView(geminiCard);

        LinearLayout appCard = NovaUi.card(this);
        appCard.addView(NovaUi.label(this, "THEME"));
        appCard.addView(NovaUi.value(this, "Dark (default)"));
        appCard.addView(NovaUi.label(this, "APP VERSION"));
        appCard.addView(NovaUi.value(this, BuildConfig.VERSION_NAME));
        appCard.addView(NovaUi.label(this, "ABOUT NOVA"));
        appCard.addView(NovaUi.value(this,
                "NOVA is a lightweight Android agent using Gemini as its reasoning brain."));
        content.addView(appCard);

        LinearLayout resetCard = NovaUi.card(this);
        resetCard.addView(NovaUi.label(this, "RESET"));

        Button clearChatButton = new Button(this);
        clearChatButton.setText("Clear chat history");
        clearChatButton.setAllCaps(false);
        clearChatButton.setBackgroundColor(NovaUi.ACCENT);
        clearChatButton.setTextColor(Color.BLACK);
        clearChatButton.setOnClickListener(v -> {
            NovaState.INSTANCE.clearChat();
            Toast.makeText(this, "Chat history cleared.", Toast.LENGTH_SHORT).show();
        });
        resetCard.addView(clearChatButton);

        content.addView(resetCard);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(scroll, scrollLp);

        root.addView(NovaUi.bottomNav(this, SettingsActivity.class));

        setContentView(root);
    }
}
