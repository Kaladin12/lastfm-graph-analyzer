package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Data
@Node
@NoArgsConstructor
public class Album {
    @Id
    private String id;
    private String name;

    @Relationship(type = "HAS_GENRE")
    public Set<Genre> genres;

    @Relationship(type = "CONTAINS")
    public Set<Track> tracks;

    public void hasGenre(Genre genre) {
        if (this.genres == null) {
            this.genres = new HashSet<>();
        }
        genres.add(genre);
    }

    public void hasTrack(Track track) {
        if (this.tracks == null) {
            this.tracks = new HashSet<>();
        }
        tracks.add(track);
    }
}
