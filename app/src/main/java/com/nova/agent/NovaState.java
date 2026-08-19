package com.nova.agent;

import java.util.ArrayList;
import java.util.List;

public class NovaState {

    public static final NovaState INSTANCE = new NovaState();

    public enum ConnectionStatus { UNKNOWN, ONLINE, ERROR }

    public String model = "gemini-3.6-flash";
    public ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;
    public String lastRequestStatus = "No requests yet.";
    public boolean apiKeyConfigured = false;

    public static class Message {
        public final boolean fromUser;
        public final String text;

        public Message(boolean fromUser, String text) {
            this.fromUser = fromUser;
            this.text = text;
        }
    }

    public final List<Message> chatHistory = new ArrayList<>();

    private NovaState() {}

    public void addUserMessage(String text) {
        chatHistory.add(new Message(true, text));
    }

    public void addNovaMessage(String text) {
        chatHistory.add(new Message(false, text));
    }

    public void clearChat() {
        chatHistory.clear();
    }
}
