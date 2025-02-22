package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document("test_artist_info")
public class LastfmArtist {
    @Id
    private String mbid;
    private String name;
    private LastfmArtistInfoResponse.Stats stats;
    private List<LastfmArtistInfoResponse.Tag> tags;
    private List<LastfmTrack> tracks;
}
