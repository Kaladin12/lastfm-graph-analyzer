package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.TrackNeo4jRepository;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.MusicRepositoryService;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.TrackNeo4jService;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.TrackMapper;
import lombok.CustomLog;

@Controller
@RequestMapping("/api/v1/loader")
@CrossOrigin("*")
public class LoadController {
  private final Logger log = org.slf4j.LoggerFactory.getLogger(LoadController.class);

  private final MusicRepositoryService musicRepositoryService;
  private final TrackNeo4jService trackNeo4jService;

  public LoadController(MusicRepositoryService musicRepositoryService, TrackNeo4jService trackNeo4jService) {
    this.trackNeo4jService = trackNeo4jService;
    this.musicRepositoryService = musicRepositoryService;
  }

  @GetMapping("/artist/{id}/track/all")
  public ResponseEntity<String> getMethodName(@PathVariable String id) {
    var tracks = musicRepositoryService.findAllTracksByArtist(id);

    tracks.forEach(track -> {
      log.info("TRACK: {}", track.getName());
      Track t = TrackMapper.fromMongoToNeo(track);
      log.info("Mapped track: {}", t);
      trackNeo4jService.saveTrack(t);
    });
    return ResponseEntity.accepted().body("YAY!");
  }

  @GetMapping("/artist/{id}/album/all")
  public ResponseEntity<String> getAlbums(@PathVariable String id) {
    return ResponseEntity.ok("");
  }

}
