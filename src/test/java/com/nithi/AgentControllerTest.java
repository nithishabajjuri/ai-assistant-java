package com.nithi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Reset API should return success true")
    public void testResetApi() throws Exception {
        mockMvc.perform(post("/api/reset"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Food API Swiggy should return swiggy URL")
    public void testFoodApiSwiggy() throws Exception {
        String requestBody = """
            {
                "food": "biryani",
                "platform": "swiggy"
            }
            """;

        mockMvc.perform(post("/api/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.reply")
                .value(org.hamcrest.Matchers
                    .containsString("swiggy")));
    }

    @Test
    @DisplayName("Food API Zomato should return zomato URL")
    public void testFoodApiZomato() throws Exception {
        String requestBody = """
            {
                "food": "pizza",
                "platform": "zomato"
            }
            """;

        mockMvc.perform(post("/api/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reply")
                .value(org.hamcrest.Matchers
                    .containsString("zomato")));
    }

    @Test
    @DisplayName("Email API without config should return error")
    public void testEmailApiNoConfig() throws Exception {
        String requestBody = """
            {
                "to": "test@gmail.com",
                "subject": "Test Subject",
                "body": "Hello this is test"
            }
            """;

        mockMvc.perform(post("/api/email")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reply")
                .value(org.hamcrest.Matchers
                    .containsString("not configured")));
    }
}
