package com.woollybonsai.craftitunes.auth;

import com.sun.net.httpserver.HttpServer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.Scanner;

public class SpotifyAuthManager {

    private static final String CLIENT_ID = "04b1101c81cc4d00958fc1485eb50f4a"; // Public ID for PKCE
    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String SCOPES = "user-read-private user-read-email user-library-read user-modify-playback-state user-read-playback-state streaming";

    private static String codeVerifier;
    private static HttpServer server;
    
    public static String accessToken = null;

    public static void startAuthFlow() {
        try {
            codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);

            startCallbackServer();

            String authUrl = "https://accounts.spotify.com/authorize" +
                    "?response_type=code" +
                    "&client_id=" + CLIENT_ID +
                    "&scope=" + SCOPES.replace(" ", "%20") +
                    "&redirect_uri=" + REDIRECT_URI.replace(":", "%3A").replace("/", "%2F") +
                    "&code_challenge_method=S256" +
                    "&code_challenge=" + codeChallenge;

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("§aOpened browser for Spotify Authentication..."), false);
                }
            } else {
                System.out.println("Desktop browse not supported. Please go to: " + authUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCallbackServer() throws Exception {
        if (server != null) {
            server.stop(0);
        }
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String responseStr = "<html><body><h1>Spotify Auth Failed</h1><p>You can close this window.</p></body></html>";
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                exchangeToken(code);
                responseStr = "<html><body><h1>Authentication Successful!</h1><p>CraftiTunes is now linked to Spotify. You may close this window and return to Minecraft.</p></body></html>";
            }
            
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes(StandardCharsets.UTF_8));
            os.close();

            // Stop server after handling
            Executors.newSingleThreadExecutor().execute(() -> {
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                server.stop(0);
            });
        });
        server.setExecutor(null);
        server.start();
    }

    private static void exchangeToken(String authCode) {
        try {
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String body = "client_id=" + CLIENT_ID +
                    "&grant_type=authorization_code" +
                    "&code=" + authCode +
                    "&redirect_uri=" + REDIRECT_URI.replace(":", "%3A").replace("/", "%2F") +
                    "&code_verifier=" + codeVerifier;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (InputStream is = conn.getInputStream(); Scanner sc = new Scanner(is, StandardCharsets.UTF_8)) {
                    String json = sc.useDelimiter("\\A").next();
                    // Basic JSON parsing without GSON for now
                    if (json.contains("\"access_token\"")) {
                        accessToken = json.split("\"access_token\":\"")[1].split("\"")[0];
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(Component.literal("§aSpotify Linked Successfully!"), false);
                        }
                        System.out.println("Spotify Token Acquired!");
                    }
                }
            } else {
                System.out.println("Token exchange failed: " + code);
                try (InputStream is = conn.getErrorStream(); Scanner sc = new Scanner(is, StandardCharsets.UTF_8)) {
                    System.out.println(sc.useDelimiter("\\A").next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private static String generateCodeChallenge(String verifier) throws Exception {
        byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
