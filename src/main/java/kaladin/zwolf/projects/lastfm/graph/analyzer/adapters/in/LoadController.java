package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.MusicRepositoryService;
import lombok.CustomLog;

@Controller
@RequestMapping("/api/v1/loader")
@CrossOrigin("*")
public class LoadController {
  private final Logger log = org.slf4j.LoggerFactory.getLogger(LoadController.class);

  private final MusicRepositoryService musicRepositoryService;

  public LoadController(MusicRepositoryService musicRepositoryService) {
    this.musicRepositoryService = musicRepositoryService;
  }

  @GetMapping("/artist/{id}/track/all")
  public void getMethodName(@PathVariable String id) {
    var tracks = musicRepositoryService.findAllTracksByArtist(id);
    tracks.forEach(track -> {
      log.info("TRACK: {}", track.getName());
    });
  }

}
