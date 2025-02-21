package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastfmTrackResponse {
    @JsonProperty("toptracks")
    private TopTracks topTracks;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopTracks {
        List<Track> tracks;
        @JsonProperty("@attr")
        private LastfmGetLibraryArtistsResponse.PageAttributes pageAttributes;
    }

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Track {
        private String mbid;
        private String name;
        private String duration;
        private String playcount;
        private LastfmGetLibraryArtistsResponse.Artist artist;
        @JsonProperty("@attr")
        private Attributes attributes;
    }

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        private String rank;
    }


}
