package com.nova.agent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView novaStatusValue;
    private TextView brainStatusValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(NovaUi.BG);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 32, 32, 16);

        TextView title = NovaUi.header(this, "NOVA");
        title.setTextSize(30);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Lightweight Android Agent • v0.3");
        subtitle.setTextColor(NovaUi.TEXT_SECONDARY);
        subtitle.setTextSize(13);
        subtitle.setPadding(0, 0, 0, 24);
        content.addView(subtitle);

        LinearLayout statusCard = NovaUi.card(this);
        statusCard.addView(NovaUi.label(this, "NOVA STATUS"));
        novaStatusValue = NovaUi.value(this, "Online");
        statusCard.addView(novaStatusValue);
        statusCard.addView(NovaUi.label(this, "GEMINI BRAIN"));
        brainStatusValue = NovaUi.value(this, "Unknown");
        statusCard.addView(brainStatusValue);
        content.addView(statusCard);

        TextView quickLabel = NovaUi.label(this, "QUICK ACTIONS");
        quickLabel.setPadding(0, 12, 0, 8);
        content.addView(quickLabel);

        content.addView(actionButton("Open Chat", v ->
                startActivity(new Intent(this, ChatActivity.class))));

        content.addView(actionButton("Open Chrome", v ->
                launchPackage("com.android.chrome", "Chrome")));

        content.addView(actionButton("Open YouTube", v ->
                launchPackage("com.google.android.youtube", "YouTube")));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(scroll, scrollLp);

        root.addView(NovaUi.bottomNav(this, MainActivity.class));

        setContentView(root);
    }

    private Button actionButton(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setBackgroundColor(NovaUi.BG_CARD);
        b.setTextColor(NovaUi.TEXT_PRIMARY);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        b.setLayoutParams(lp);

        b.setOnClickListener(listener);
        return b;
    }

    private void launchPackage(String packageName, String appName) {
        PackageManager pm = getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage(packageName);

        if (launch == null) {
            Toast.makeText(this, appName + " is not installed.", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(launch);
    }

    private void refreshStatus() {
        boolean keyConfigured = BuildConfig.GEMINI_API_KEY != null
                && !BuildConfig.GEMINI_API_KEY.trim().isEmpty();
        NovaState.INSTANCE.apiKeyConfigured = keyConfigured;

        novaStatusValue.setText(keyConfigured ? "Online" : "Online (no API key)");

        switch (NovaState.INSTANCE.connectionStatus) {
            case ONLINE:
                brainStatusValue.setText("Gemini — Connected");
                break;
            case ERROR:
                brainStatusValue.setText("Gemini — Error");
                break;
            default:
                brainStatusValue.setText(keyConfigured ? "Gemini — Ready" : "Gemini — Not configured");
        }
    }
}
