package kaladin.zwolf.projects.lastfm.graph.analyzer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document("artist_info")
public class LastfmArtistInfo {
    private Artist artist;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artist {
        private String name;
        @Id
        private String mbid;
        private Stats stats;
        private Tags tags;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private String listeners;
        private String playcount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tags {
        private List<Tag> tag;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        private String name;
    }
}
