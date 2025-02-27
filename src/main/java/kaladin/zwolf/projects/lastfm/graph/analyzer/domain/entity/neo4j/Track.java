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
public class Track {
  @Id
  private String mbid;
  private String title;
  private int playCount;
  private int duration;
  private int rank;

  @Relationship(type = "PERFORMED_BY")
  public Set<Artist> artists;

  @Relationship(type = "HAS_GENRE")
  public Set<Genre> genres;

  public void performedBy(Artist artist) {
    if (this.artists == null) {
      this.artists = new HashSet<>();
    }
    artists.add(artist);
  }

  public void hasGenre(Genre genre) {
    if (this.genres == null) {
      this.genres = new HashSet<>();
    }
    genres.add(genre);
  }

}
