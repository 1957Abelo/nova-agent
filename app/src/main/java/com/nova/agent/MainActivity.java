package com.nova.agent;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView response;
    private EditText commandInput;
    private SharedPreferences skills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        skills = getSharedPreferences("nova_skills", Context.MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 32);
        root.setBackgroundColor(Color.rgb(8, 12, 16));

        TextView title = new TextView(this);
        title.setText("NOVA");
        title.setTextColor(Color.rgb(0, 229, 160));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);

        TextView subtitle = new TextView(this);
        subtitle.setText("Lightweight Android Agent • v0.1");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 30);

        response = new TextView(this);
        response.setText("NOVA online.\n\nTry:\n• hello\n• open chrome\n• open youtube");
        response.setTextColor(Color.WHITE);
        response.setTextSize(17);
        response.setPadding(20, 20, 20, 20);

        commandInput = new EditText(this);
        commandInput.setHint("Tell NOVA what to do...");
        commandInput.setSingleLine(true);
        commandInput.setTextColor(Color.WHITE);
        commandInput.setHintTextColor(Color.GRAY);

        Button execute = new Button(this);
        execute.setText("EXECUTE");

        execute.setOnClickListener(v -> {
            String command = commandInput.getText().toString().trim();
            routeCommand(command);
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(response,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(commandInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        root.addView(execute,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void routeCommand(String command) {
        String c = command.toLowerCase();

        if (c.equals("hello") || c.equals("hi")) {
            respond("Hello. I'm NOVA.\n\nI'm ready.");
            return;
        }

        if (c.equals("open chrome")) {
            openApp("com.android.chrome", "Chrome");
            return;
        }

        if (c.equals("open youtube")) {
            openApp("com.google.android.youtube", "YouTube");
            return;
        }

        if (c.startsWith("learn ")) {
            String skill = command.substring(6).trim();

            if (!skill.isEmpty()) {
                skills.edit()
                        .putBoolean(skill, true)
                        .apply();

                respond("Skill saved:\n" + skill);
            }

            return;
        }

        respond("I don't know that command yet.\n\n"
                + "Available commands:\n"
                + "hello\n"
                + "open chrome\n"
                + "open youtube\n"
                + "learn <skill>");
    }

    private void openApp(String packageName, String name) {
        PackageManager pm = getPackageManager();

        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            startActivity(intent);
            respond("Opening " + name + "...");
        } else {
            respond(name + " is not installed.");
        }
    }

    private void respond(String text) {
        response.setText("NOVA\n\n" + text);
    }
}
