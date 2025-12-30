package mygame.dialogue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DialogueClient {

    private final HttpClient client;
    private final String baseUrl;

    public DialogueClient(String baseUrl) {
        this.client = HttpClient.newHttpClient();
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    public String talkToNpc(String npcId, String playerId, String message) throws Exception {
        String json = """
            {
              "npcId": "%s",
              "playerId": "%s",
              "message": "%s"
            }
            """.formatted(
                    escape(npcId),
                    escape(playerId == null ? "" : playerId),
                    escape(message)
            );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/dialogue/npc"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Dialogue server error: " + response.statusCode()
                    + " -> " + response.body());
            return "(the witch is silent; something went wrong...)";
        }

        // JSON parser to grab "reply": "..."
        String body = response.body();
        int idx = body.indexOf("\"reply\"");
        if (idx < 0) return "(no reply field in JSON)";
        int colon = body.indexOf(':', idx);
        int firstQuote = body.indexOf('"', colon + 1);
        int secondQuote = body.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return "(malformed reply JSON)";
        return body.substring(firstQuote + 1, secondQuote);
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
