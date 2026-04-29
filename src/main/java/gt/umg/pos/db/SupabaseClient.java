package gt.umg.pos.db;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Optional;

public class SupabaseClient {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static Optional<String> get(String endpoint) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey",        SupabaseConfig.SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .header("Content-Type",  "application/json")
                    .GET().build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) return Optional.of(res.body());
            System.err.println("[Supabase GET] Error " + res.statusCode() + ": " + res.body());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("[Supabase GET] " + e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<String> upsert(String endpoint, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "?on_conflict=codigo"))  // <- agregar esto
                    .header("apikey",        SupabaseConfig.SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .header("Content-Type",  "application/json")
                    .header("Prefer",        "resolution=merge-duplicates,return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200 || res.statusCode() == 201) return Optional.of(res.body());
            System.err.println("[Supabase UPSERT] Error " + res.statusCode() + ": " + res.body());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("[Supabase UPSERT] " + e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<String> post(String endpoint, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey",        SupabaseConfig.SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .header("Content-Type",  "application/json")
                    .header("Prefer",        "return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 201) return Optional.of(res.body());
            System.err.println("[Supabase POST] Error " + res.statusCode() + ": " + res.body());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("[Supabase POST] " + e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean patch(String endpoint, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey",        SupabaseConfig.SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .header("Content-Type",  "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200 || res.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[Supabase PATCH] " + e.getMessage());
            return false;
        }
    }
}
