package com.nithi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AgentController.class)
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaudeService claude;

    @MockBean
    private ToolsService tools;

    @Test
    @DisplayName("Reset API should return success")
    public void testResetApi() throws Exception {
        mockMvc.perform(post("/api/reset"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Food API should return URL")
    public void testFoodApi() throws Exception {
        when(tools.getFoodUrl("biryani", "swiggy"))
            .thenReturn("https://swiggy.com/search?biryani");

        String requestBody = """
            {
                "food": "biryani",
                "platform": "swiggy"
            }
            """;

        mockMvc.perform(post("/api/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Email API should return response")
    public void testEmailApi() throws Exception {
        when(tools.sendEmail(any(), any(), any()))
            .thenReturn("❌ Email not configured");

        String requestBody = """
            {
                "to": "test@gmail.com",
                "subject": "Test",
                "body": "Hello"
            }
            """;

        mockMvc.perform(post("/api/email")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk());
    }
}
