package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Artist;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ArtistNeo4jRepository extends Neo4jRepository<Artist, String> {
}
