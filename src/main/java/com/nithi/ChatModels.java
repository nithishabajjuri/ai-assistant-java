package com.nithi;

public class ChatModels {

    public record ChatRequest(String message) {}

    public record ChatResponse(String reply, boolean success, String error) {
        public static ChatResponse ok(String reply) {
            return new ChatResponse(reply, true, null);
        }
        public static ChatResponse error(String error) {
            return new ChatResponse(null, false, error);
        }
    }

    public record EmailRequest(String to, String subject, String body) {}
    public record CallRequest(String to, String message) {}
    public record FoodRequest(String food, String platform) {}
}
