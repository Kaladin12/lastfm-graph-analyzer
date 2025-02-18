package kaladin.zwolf.projects.lastfm.graph.analyzer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastfmGetLibraryArtistsResponse {
    private Artists artists;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artists {
        private List<Artist> artist;
        @JsonProperty("@attr")
        private Attributes attributes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artist {
        private String mbid;
        private String name;
        private String playcount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        private String totalPages;
        private String page;
        private String perPage;
        private String total;
    }
}
