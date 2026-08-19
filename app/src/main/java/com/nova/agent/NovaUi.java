package com.nova.agent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NovaUi {

    public static final int BG = Color.rgb(12, 12, 18);
    public static final int BG_CARD = Color.rgb(20, 20, 28);
    public static final int ACCENT = Color.parseColor("#00E5A0");
    public static final int TEXT_PRIMARY = Color.WHITE;
    public static final int TEXT_SECONDARY = Color.LTGRAY;
    public static final int BUBBLE_USER = Color.parseColor("#1B3B33");
    public static final int BUBBLE_NOVA = Color.rgb(24, 24, 32);

    public static TextView header(Activity activity, String title) {
        TextView t = new TextView(activity);
        t.setText(title);
        t.setTextSize(24);
        t.setTextColor(TEXT_PRIMARY);
        t.setPadding(0, 16, 0, 16);
        return t;
    }

    public static LinearLayout card(Activity activity) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(28, 28, 28, 28);
        card.setBackgroundColor(BG_CARD);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        card.setLayoutParams(lp);

        return card;
    }

    public static TextView label(Activity activity, String text) {
        TextView t = new TextView(activity);
        t.setText(text);
        t.setTextColor(TEXT_SECONDARY);
        t.setTextSize(12);
        return t;
    }

    public static TextView value(Activity activity, String text) {
        TextView t = new TextView(activity);
        t.setText(text);
        t.setTextColor(TEXT_PRIMARY);
        t.setTextSize(16);
        t.setPadding(0, 2, 0, 16);
        return t;
    }

    public static LinearLayout bottomNav(Activity activity, Class<?> current) {
        LinearLayout nav = new LinearLayout(activity);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(BG_CARD);
        nav.setPadding(0, 16, 0, 16);

        nav.addView(navButton(activity, "Home", MainActivity.class, current));
        nav.addView(navButton(activity, "Chat", ChatActivity.class, current));
        nav.addView(navButton(activity, "Brain", BrainActivity.class, current));
        nav.addView(navButton(activity, "Settings", SettingsActivity.class, current));

        return nav;
    }

    private static Button navButton(Activity activity, String label, Class<?> target, Class<?> current) {
        Button b = new Button(activity);
        b.setText(label);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setBackgroundColor(target.equals(current) ? ACCENT : BG_CARD);
        b.setTextColor(target.equals(current) ? Color.BLACK : TEXT_PRIMARY);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        b.setLayoutParams(lp);

        b.setOnClickListener(v -> {
            if (!target.equals(current)) {
                activity.startActivity(new Intent(activity, target));
                activity.overridePendingTransition(0, 0);
            }
        });

        return b;
    }
}
