package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.*;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1/loader")
@CrossOrigin("*")
public class LoadAdapter {
  private final Logger log = org.slf4j.LoggerFactory.getLogger(LoadAdapter.class);

  private final MusicRepositoryService musicRepositoryService;
  private final LoaderService loaderService;

  public LoadAdapter(MusicRepositoryService musicRepositoryService, LoaderService loaderService) {
    this.musicRepositoryService = musicRepositoryService;
    this.loaderService = loaderService;
  }

  @GetMapping("/artist/{id}/track/all")
  public ResponseEntity<String> getMethodName(@PathVariable String id) {
    loaderService.findAndLoadTracks(id);
    return ResponseEntity.accepted().body("YAY!");
  }

  @GetMapping("/artist/{id}/album/all")
  public ResponseEntity<String> getAlbums(@PathVariable String id) {
    loaderService.findAndLoadAlbums(id);
    return ResponseEntity.ok("");
  }

  @GetMapping("/artist/{id}")
  public ResponseEntity<String> getArtist(@PathVariable String id) {
    loaderService.findAndLoadArtist(id);
    return ResponseEntity.ok("");
  }

  @GetMapping("/artist/all")
  public ResponseEntity<Set<LastfmArtist>> getArtists() {
    var e = musicRepositoryService.findAllArtists(PageRequest.of(1, 10));
    // e.getTotalPages();
    for (LastfmArtist current : e.getContent()) {
      String id = current.getMbid();
      loaderService.findAndLoadArtist(id);
    }
    return ResponseEntity.ok(e.get().collect(Collectors.toSet()));
  }
}
