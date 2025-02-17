package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.LastFmApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lastfm")
@CrossOrigin("*")
public class LastfmAdapter {
    private final Logger log = LoggerFactory.getLogger(LastfmAdapter.class);

    private LastFmApiService lastFmApiService;

    public LastfmAdapter(LastFmApiService lastFmApiService) {
        this.lastFmApiService = lastFmApiService;
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam("token") String token) {
        log.info("RECEIVED TOKEN: {}", token);
        String sessionKey = lastFmApiService.getSessionKey(token);
        log.info("RETRIEVED SESSION KEY: {}", sessionKey);
    }

    @GetMapping("/artist/{id}")
    public void getArtist(@PathVariable String id) {
        LastfmArtistInfo artist = lastFmApiService.getArtistInfo(id);
        log.info("ARTIST INFO: {}" , artist);
    }
}
