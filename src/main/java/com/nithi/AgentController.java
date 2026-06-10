package com.nithi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Nithi Agent", 
     description = "AI Agent APIs for chat, email, calls and food")
public class AgentController {

    private final ClaudeService claude;
    private final ToolsService tools;

    public AgentController(ClaudeService claude,
                           ToolsService tools) {
        this.claude = claude;
        this.tools  = tools;
    }

    @Operation(
        summary = "Chat with Nithi AI",
        description = "Send a natural language message and get AI response")
    @ApiResponse(responseCode = "200",
                 description = "AI response generated")
    @PostMapping("/chat")
    public ChatModels.ChatResponse chat(
            @RequestBody ChatModels.ChatRequest req) {
        try {
            return ChatModels.ChatResponse
                .ok(claude.chat(req.message()));
        } catch (Exception e) {
            return ChatModels.ChatResponse
                .error(e.getMessage());
        }
    }

    @Operation(
        summary = "Send Email",
        description = "Send email via Gmail SMTP")
    @PostMapping("/email")
    public ChatModels.ChatResponse email(
            @RequestBody ChatModels.EmailRequest req) {
        String result = tools.sendEmail(
            req.to(), req.subject(), req.body());
        return ChatModels.ChatResponse.ok(result);
    }

    @Operation(
        summary = "Make Phone Call",
        description = "Make call via Twilio API")
    @PostMapping("/call")
    public ChatModels.ChatResponse call(
            @RequestBody ChatModels.CallRequest req) {
        String result = tools.makeCall(
            req.to(), req.message());
        return ChatModels.ChatResponse.ok(result);
    }

    @Operation(
        summary = "Order Food",
        description = "Get food delivery URL for Swiggy, Zomato etc")
    @PostMapping("/food")
    public ChatModels.ChatResponse food(
            @RequestBody ChatModels.FoodRequest req) {
        String url = tools.getFoodUrl(
            req.food(), req.platform());
        return ChatModels.ChatResponse.ok(url);
    }

    @Operation(
        summary = "Reset Conversation",
        description = "Clear conversation history")
    @PostMapping("/reset")
    public ChatModels.ChatResponse reset() {
        claude.resetHistory();
        return ChatModels.ChatResponse
            .ok("Conversation reset.");
    }
}
