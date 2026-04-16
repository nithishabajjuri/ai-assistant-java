package com.nithi;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ClaudeService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-20250514";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String SYSTEM_PROMPT = """
        You are Nithi, a smart and friendly personal AI assistant.
        You help with tasks, answer questions, give advice, and have natural conversations.
        Be warm, concise, and helpful. Keep responses short and clear — this is a chat interface.
        
        When the user asks you to send an email, make a call, or order food — use the appropriate tool.
        Always confirm what you did after using a tool.
        """;

    private static final String TOOLS_JSON = """
        [
          {
            "name": "send_email",
            "description": "Send an email to someone. Use when user asks to send/write/compose an email.",
            "input_schema": {
              "type": "object",
              "properties": {
                "to":      { "type": "string", "description": "Recipient email address" },
                "subject": { "type": "string", "description": "Email subject line" },
                "body":    { "type": "string", "description": "Email body content" }
              },
              "required": ["to", "subject", "body"]
            }
          },
          {
            "name": "make_call",
            "description": "Make a phone call and speak a message. Use when user asks to call someone.",
            "input_schema": {
              "type": "object",
              "properties": {
                "to":      { "type": "string", "description": "Phone number e.g. +911234567890" },
                "message": { "type": "string", "description": "Message to speak when answered" }
              },
              "required": ["to", "message"]
            }
          },
          {
            "name": "order_food",
            "description": "Open a food delivery app. Use when user asks to order food.",
            "input_schema": {
              "type": "object",
              "properties": {
                "food":     { "type": "string", "description": "Food item to search for" },
                "platform": {
                  "type": "string",
                  "enum": ["swiggy", "zomato", "ubereats", "doordash", "grubhub"],
                  "description": "Food delivery platform"
                }
              },
              "required": ["food"]
            }
          },
          {
            "name": "search_food",
            "description": "Search for best restaurants near user and show results in chat. Use when user asks for best food options, recommendations, or what's available near them.",
            "input_schema": {
              "type": "object",
              "properties": {
                "food":     { "type": "string", "description": "Food or cuisine to search for" },
                "location": { "type": "string", "description": "Location or area (optional)" }
              },
              "required": ["food"]
            }
          }
        ]
        """;

    private final OkHttpClient http = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build();

    private final Gson gson = new Gson();
    private final List<Object> history = new ArrayList<>();
    private final String apiKey = System.getenv("ANTHROPIC_API_KEY");
    private final ToolsService tools;

    public ClaudeService(ToolsService tools) {
        this.tools = tools;
    }

    public String chat(String userMessage) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("ANTHROPIC_API_KEY not set");
        }

        history.add(Map.of("role", "user", "content", userMessage));

        for (int i = 0; i < 5; i++) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 512);
            body.put("system", SYSTEM_PROMPT);
            body.put("tools", JsonParser.parseString(TOOLS_JSON));
            body.put("messages", history);

            Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(gson.toJson(body), JSON))
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .build();

            try (Response response = http.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    throw new IOException("API error " + response.code() + ": " + responseBody);
                }

                JsonObject json   = JsonParser.parseString(responseBody).getAsJsonObject();
                String stopReason = json.get("stop_reason").getAsString();
                JsonArray content = json.getAsJsonArray("content");

                history.add(Map.of("role", "assistant", "content", gsonList(content)));

                if ("end_turn".equals(stopReason)) {
                    for (JsonElement el : content) {
                        JsonObject block = el.getAsJsonObject();
                        if ("text".equals(block.get("type").getAsString())) {
                            String text = block.get("text").getAsString();
                            if (history.size() > 40) history.subList(0, 2).clear();
                            return text;
                        }
                    }
                }

                if ("tool_use".equals(stopReason)) {
                    List<Map<String, Object>> toolResults = new ArrayList<>();
                    for (JsonElement el : content) {
                        JsonObject block = el.getAsJsonObject();
                        if ("tool_use".equals(block.get("type").getAsString())) {
                            String toolName  = block.get("name").getAsString();
                            String toolUseId = block.get("id").getAsString();
                            JsonObject input = block.getAsJsonObject("input");
                            String result    = runTool(toolName, input);
                            toolResults.add(Map.of(
                                "type",        "tool_result",
                                "tool_use_id", toolUseId,
                                "content",     result
                            ));
                        }
                    }
                    history.add(Map.of("role", "user", "content", toolResults));
                }
            }
        }

        return "Sorry, I couldn't complete that request.";
    }

    private String runTool(String name, JsonObject input) {
        System.out.println("🔧 Running tool: " + name);
        return switch (name) {
            case "send_email" -> tools.sendEmail(
                input.get("to").getAsString(),
                input.get("subject").getAsString(),
                input.get("body").getAsString()
            );
            case "make_call" -> tools.makeCall(
                input.get("to").getAsString(),
                input.get("message").getAsString()
            );
            case "search_food" -> tools.searchFood(
                input.get("food").getAsString(),
                input.has("location") ? input.get("location").getAsString() : ""
            );
            case "order_food" -> {
                String platform = input.has("platform")
                    ? input.get("platform").getAsString() : "swiggy";
                yield tools.getFoodUrl(input.get("food").getAsString(), platform);
            }
            default -> "Unknown tool: " + name;
        };
    }

    private List<Map<String, Object>> gsonList(JsonArray arr) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JsonElement el : arr) {
            list.add(gson.fromJson(el, Map.class));
        }
        return list;
    }

    public void resetHistory() {
        history.clear();
    }
}
