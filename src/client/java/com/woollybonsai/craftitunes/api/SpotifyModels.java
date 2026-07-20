package com.woollybonsai.craftitunes.api;

import java.util.List;

public class SpotifyModels {

    public record PlaylistsResponse(List<PlaylistItem> items) {}

    public record PlaylistItem(String id, String name, List<Image> images) {
        public String getCoverUrl() {
            return (images != null && !images.isEmpty()) ? images.getFirst().url() : null;
        }
    }

    public record Image(String url) {}

    public record PlaylistTracksResponse(List<TrackItem> items) {}

    public record TrackItem(Track item) {}

    public record Track(String id, String name, List<Artist> artists) {
        public String getArtistNames() {
            if (artists == null || artists.isEmpty()) return "Unknown Artist";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < artists.size(); i++) {
                sb.append(artists.get(i).name());
                if (i < artists.size() - 1) sb.append(", ");
            }
            return sb.toString();
        }
    }

    public record Artist(String name) {}
}
