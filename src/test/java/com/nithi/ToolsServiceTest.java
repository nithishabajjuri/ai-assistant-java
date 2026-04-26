package com.nithi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ToolsServiceTest {

    private ToolsService toolsService;

    @BeforeEach
    public void setUp() {
        toolsService = new ToolsService();
    }

    @Test
    @DisplayName("Swiggy URL should contain swiggy.com")
    public void testGetFoodUrlSwiggy() {
        String url = toolsService.getFoodUrl("biryani", "swiggy");
        assertTrue(url.contains("swiggy.com"));
    }

    @Test
    @DisplayName("Zomato URL should contain zomato.com")
    public void testGetFoodUrlZomato() {
        String url = toolsService.getFoodUrl("pizza", "zomato");
        assertTrue(url.contains("zomato.com"));
        assertTrue(url.contains("pizza"));
    }

    @Test
    @DisplayName("Unknown platform should return Uber Eats URL")
    public void testGetFoodUrlDefault() {
        String url = toolsService.getFoodUrl("burger", "unknown");
        assertTrue(url.contains("ubereats.com"));
    }

    @Test
    @DisplayName("Food URL should never be empty")
    public void testGetFoodUrlNotEmpty() {
        String url = toolsService.getFoodUrl("dosa", "swiggy");
        assertNotNull(url);
        assertFalse(url.isEmpty());
    }

    @Test
    @DisplayName("Email without config should return error")
    public void testSendEmailNoConfig() {
        String result = toolsService.sendEmail(
            "test@gmail.com",
            "Test Subject",
            "Hello this is test"
        );
        assertTrue(result.contains("not configured"));
    }

    @Test
    @DisplayName("Call without config should return error")
    public void testMakeCallNoConfig() {
        String result = toolsService.makeCall(
            "+911234567890",
            "Hello!"
        );
        assertTrue(result.contains("not configured"));
    }
}
