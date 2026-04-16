package com.nithi;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AgentController {

    private final ClaudeService claude;
    private final ToolsService tools;

    public AgentController(ClaudeService claude, ToolsService tools) {
        this.claude = claude;
        this.tools  = tools;
    }

    @PostMapping("/chat")
    public ChatModels.ChatResponse chat(@RequestBody ChatModels.ChatRequest req) {
        try {
            String reply = claude.chat(req.message());
            return ChatModels.ChatResponse.ok(reply);
        } catch (Exception e) {
            return ChatModels.ChatResponse.error(e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ChatModels.ChatResponse reset() {
        claude.resetHistory();
        return ChatModels.ChatResponse.ok("Conversation reset.");
    }

    @PostMapping("/email")
    public ChatModels.ChatResponse email(@RequestBody ChatModels.EmailRequest req) {
        String result = tools.sendEmail(req.to(), req.subject(), req.body());
        return ChatModels.ChatResponse.ok(result);
    }

    @PostMapping("/call")
    public ChatModels.ChatResponse call(@RequestBody ChatModels.CallRequest req) {
        String result = tools.makeCall(req.to(), req.message());
        return ChatModels.ChatResponse.ok(result);
    }

    @PostMapping("/food")
    public ChatModels.ChatResponse food(@RequestBody ChatModels.FoodRequest req) {
        String url = tools.getFoodUrl(req.food(), req.platform());
        return ChatModels.ChatResponse.ok(url);
    }
}
