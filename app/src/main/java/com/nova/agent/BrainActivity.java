package com.nova.agent;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class BrainActivity extends Activity {

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

        content.addView(NovaUi.header(this, "Brain"));

        boolean keyConfigured = BuildConfig.GEMINI_API_KEY != null
                && !BuildConfig.GEMINI_API_KEY.trim().isEmpty();
        NovaState.INSTANCE.apiKeyConfigured = keyConfigured;

        LinearLayout card = NovaUi.card(this);
        card.addView(NovaUi.label(this, "BRAIN"));
        card.addView(NovaUi.value(this, "Gemini"));

        card.addView(NovaUi.label(this, "MODEL"));
        card.addView(NovaUi.value(this, NovaState.INSTANCE.model));

        card.addView(NovaUi.label(this, "CONNECTION STATUS"));
        card.addView(NovaUi.value(this, NovaState.INSTANCE.connectionStatus.name()));

        card.addView(NovaUi.label(this, "LAST REQUEST STATUS"));
        card.addView(NovaUi.value(this, NovaState.INSTANCE.lastRequestStatus));

        card.addView(NovaUi.label(this, "API KEY STATUS"));
        card.addView(NovaUi.value(this, keyConfigured ? "Configured" : "Not configured"));

        content.addView(card);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(scroll, scrollLp);

        root.addView(NovaUi.bottomNav(this, BrainActivity.class));

        setContentView(root);
    }
}
