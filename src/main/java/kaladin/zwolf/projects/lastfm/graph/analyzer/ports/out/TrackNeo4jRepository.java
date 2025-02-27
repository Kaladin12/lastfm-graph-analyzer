package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;

public interface TrackNeo4jRepository extends Neo4jRepository<Track, String> {
  Optional<Track> findTrackByMbid(String mbid);

  Optional<Track> findTrackByTitle(String title);
}
