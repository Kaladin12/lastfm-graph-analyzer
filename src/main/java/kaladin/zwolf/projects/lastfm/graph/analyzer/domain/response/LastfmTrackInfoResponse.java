package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastfmTrackInfoResponse {
    private Track track;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Track {
        private String name;
        private String mbid;
        private Album album;
        @JsonProperty("toptags")
        private LastfmArtistInfoResponse.Tags tags;
        @JsonProperty("userplaycount")
        private String userPlayCount;
    }

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Album {
        private String title;
        private String mbid;
    }
}
