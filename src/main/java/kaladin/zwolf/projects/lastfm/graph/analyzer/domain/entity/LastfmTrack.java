package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LastfmTrack {
    private String mbid;
    private String name;
    private int duration;
    private int playcount;
    private int rank; // perhaps should be a long, but we'll see
    private Album album;
    private List<LastfmArtistInfoResponse.Tag> tags;
    private boolean generatedMbid = false;

    @Data
    @Builder
    public static class Album {
        private String mbid;
        private String name;
        private List<LastfmArtistInfoResponse.Tag> tags;
    }
}
