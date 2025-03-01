package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.TrackNeo4jRepository;

@Service
public class TrackNeo4jService {
  private final Logger log = LoggerFactory.getLogger(TrackNeo4jService.class);

  private final TrackNeo4jRepository trackNeo4jRepository;

  public TrackNeo4jService(TrackNeo4jRepository trackNeo4jRepository) {
    this.trackNeo4jRepository = trackNeo4jRepository;
  }

  public void saveTrack(Track track) {
    trackNeo4jRepository.save(track);
  }

  public Optional<Track> findTrackByMbid(String mbid) {
    return trackNeo4jRepository.findTrackByMbid(mbid);
  }
}
