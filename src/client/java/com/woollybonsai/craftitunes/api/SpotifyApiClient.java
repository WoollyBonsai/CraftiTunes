package com.woollybonsai.craftitunes.api;

import com.google.gson.Gson;
import com.woollybonsai.craftitunes.auth.SpotifyAuthManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpotifyApiClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    public static CompletableFuture<List<SpotifyModels.PlaylistItem>> getUserPlaylists() {
        if (SpotifyAuthManager.accessToken == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/playlists"))
                .header("Authorization", "Bearer " + SpotifyAuthManager.accessToken)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        SpotifyModels.PlaylistsResponse parsed = gson.fromJson(response.body(), SpotifyModels.PlaylistsResponse.class);
                        return parsed != null && parsed.items() != null ? parsed.items() : Collections.<SpotifyModels.PlaylistItem>emptyList();
                    } else {
                        System.err.println("Failed to fetch playlists: " + response.statusCode() + " " + response.body());
                        return Collections.<SpotifyModels.PlaylistItem>emptyList();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return Collections.<SpotifyModels.PlaylistItem>emptyList();
                });
    }

    public static CompletableFuture<List<SpotifyModels.TrackItem>> getPlaylistTracks(String playlistId) {
        if (SpotifyAuthManager.accessToken == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/items"))
                .header("Authorization", "Bearer " + SpotifyAuthManager.accessToken)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        SpotifyModels.PlaylistTracksResponse parsed = gson.fromJson(response.body(), SpotifyModels.PlaylistTracksResponse.class);
                        if (parsed == null || parsed.items() == null || parsed.items().isEmpty()) {
                            System.out.println("Warning: Parsed tracks are empty.");
                        }
                        return parsed != null && parsed.items() != null ? parsed.items() : Collections.<SpotifyModels.TrackItem>emptyList();
                    } else {
                        System.err.println("Failed to fetch tracks: " + response.statusCode() + " " + response.body());
                        return Collections.<SpotifyModels.TrackItem>emptyList();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return Collections.<SpotifyModels.TrackItem>emptyList();
                });
    }
}
