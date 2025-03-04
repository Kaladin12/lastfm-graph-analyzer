package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Album;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface AlbumNeo4jRepository extends Neo4jRepository<Album, String> {
    Optional<Album> findAlbumById(String id);
}
