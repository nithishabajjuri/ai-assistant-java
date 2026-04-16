package com.nithi;

import com.google.gson.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Service
public class ToolsService {

    private final OkHttpClient http = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();
    private final Gson gson = new Gson();

    // ── Email ─────────────────────────────────────────────────────────────────

    public String sendEmail(String to, String subject, String body) {
        String emailAddr = System.getenv("EMAIL_ADDRESS");
        String emailPass = System.getenv("EMAIL_PASSWORD");
        String smtpHost  = Optional.ofNullable(System.getenv("SMTP_HOST")).orElse("smtp.gmail.com");
        int smtpPort     = Integer.parseInt(Optional.ofNullable(System.getenv("SMTP_PORT")).orElse("587"));

        if (emailAddr == null || emailPass == null) {
            return "❌ Email not configured. Set EMAIL_ADDRESS and EMAIL_PASSWORD environment variables.";
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);

            Session session = Session.getInstance(props,new jakarta.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAddr, emailPass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(emailAddr));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);

            return "✅ Email sent to " + to;
        } catch (Exception e) {
            return "❌ Failed to send email: " + e.getMessage();
        }
    }

    // ── Phone Call ────────────────────────────────────────────────────────────

    public String makeCall(String to, String message) {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken  = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

        if (accountSid == null || authToken == null || fromNumber == null) {
            return "❌ Twilio not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER.";
        }

        try {
            Twilio.init(accountSid, authToken);
            Call call = Call.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                new Twiml("<Response><Say>" + message + "</Say></Response>")
            ).create();
            return "✅ Call initiated to " + to + " (SID: " + call.getSid() + ")";
        } catch (Exception e) {
            return "❌ Call failed: " + e.getMessage();
        }
    }

    // ── Food Search (Google Places) ───────────────────────────────────────────

    public String searchFood(String food, String location) {
        String googleKey = System.getenv("GOOGLE_PLACES_API_KEY");

        if (googleKey == null || googleKey.isBlank()) {
            // Fallback — return food delivery URLs if no Google key
            return getFoodUrl(food, "swiggy");
        }

        try {
            String query = URLEncoder.encode(food + " restaurant " + (location != null ? location : ""), StandardCharsets.UTF_8);
            String url   = "https://maps.googleapis.com/maps/api/place/textsearch/json"
                         + "?query=" + query
                         + "&type=restaurant"
                         + "&key=" + googleKey;

            Request request = new Request.Builder().url(url).build();

            try (Response response = http.newCall(request).execute()) {
                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray results = json.getAsJsonArray("results");

                if (results == null || results.size() == 0) {
                    return "No restaurants found for '" + food + "' near you.";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("🍽️ Best options for **").append(food).append("** near you:\n\n");

                int count = Math.min(5, results.size());
                for (int i = 0; i < count; i++) {
                    JsonObject place = results.get(i).getAsJsonObject();
                    String name    = place.get("name").getAsString();
                    double rating  = place.has("rating") ? place.get("rating").getAsDouble() : 0;
                    String address = place.has("formatted_address")
                        ? place.get("formatted_address").getAsString() : "Address not available";
                    boolean open   = place.has("opening_hours") &&
                        place.getAsJsonObject("opening_hours").has("open_now") &&
                        place.getAsJsonObject("opening_hours").get("open_now").getAsBoolean();

                    String stars = "⭐".repeat((int) Math.round(rating));
                    String status = open ? "🟢 Open" : "🔴 Closed";

                    sb.append(i + 1).append(". **").append(name).append("**\n");
                    sb.append("   ").append(stars).append(" ").append(rating).append(" | ").append(status).append("\n");
                    sb.append("   📍 ").append(address).append("\n\n");
                }

                sb.append("Want me to open Swiggy or Zomato to order from any of these?");
                return sb.toString();
            }
        } catch (Exception e) {
            return "❌ Food search error: " + e.getMessage();
        }
    }

    // ── Food Order URL ────────────────────────────────────────────────────────

    public String getFoodUrl(String food, String platform) {
        String encoded = URLEncoder.encode(food, StandardCharsets.UTF_8);
        return switch (platform.toLowerCase()) {
            case "zomato"   -> "https://www.zomato.com/search?q=" + encoded;
            case "swiggy"   -> "https://www.swiggy.com/search?query=" + encoded;
            case "doordash" -> "https://www.doordash.com/search/store/" + encoded + "/";
            case "grubhub"  -> "https://www.grubhub.com/search?queryText=" + encoded;
            default         -> "https://www.ubereats.com/search?q=" + encoded;
        };
    }
}
