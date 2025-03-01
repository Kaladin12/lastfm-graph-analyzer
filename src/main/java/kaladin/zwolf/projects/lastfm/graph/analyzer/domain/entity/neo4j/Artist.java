package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j;

import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse.Tag;
import lombok.Data;

@Data
@Node
public class Artist {
  @Id
  private String mbid;
  private String name;

  @Relationship("HAS_GENRE")
  public Set<LastfmArtistInfoResponse.Tag> tags;

  @Relationship("RELEASED")
  public Set<Album> albums;
}
