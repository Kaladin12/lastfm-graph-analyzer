package kaladin.zwolf.projects.lastfm.graph.analyzer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document("artist_info")
public class LastfmArtist {
    @Id
    private String mbid;
    private String name;
    private LastfmArtistInfoResponse.Stats stats;
    private List<LastfmArtistInfoResponse.Tag> tags;
    private List<Track> tracks;

    @Data
    @Builder
    public static class Track {
        private String mbid;
        private String name;
        private int duration;
        private int playcount;
        private Album album;
    }

    @Data
    @Builder
    public static class Album {
        private String mbid;
        private String name;
        private List<LastfmArtistInfoResponse.Tag> tags;
    }
}
