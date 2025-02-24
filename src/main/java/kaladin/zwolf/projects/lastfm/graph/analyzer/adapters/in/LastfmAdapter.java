package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.LastFmArtistApiService;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.LastFmTrackApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lastfm")
@CrossOrigin("*")
@EnableAsync
public class LastfmAdapter {
    private final Logger log = LoggerFactory.getLogger(LastfmAdapter.class);

    private LastFmArtistApiService lastFmArtistApiService;
    private LastFmTrackApiService lastFmTrackApiService;

    public LastfmAdapter(LastFmArtistApiService lastFmArtistApiService, LastFmTrackApiService lastFmTrackApiService) {
        this.lastFmArtistApiService = lastFmArtistApiService;
        this.lastFmTrackApiService = lastFmTrackApiService;
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam("token") String token) {
        log.info("RECEIVED TOKEN: {}", token);
        String sessionKey = lastFmArtistApiService.getSessionKey(token);
        log.info("RETRIEVED SESSION KEY: {}", sessionKey);
    }

    @GetMapping("/artist/{id}")
    public void getArtist(@PathVariable String id) {
        LastfmArtist artist = lastFmArtistApiService.getArtistInfo(id);
        if (artist != null) {
            log.info("RETRIEVED ARTIST: {}", artist);
        }
    }

    @GetMapping("/track/top/{username}")
    public String getTrackTop(@PathVariable String username, @RequestParam("period") String period) {
        lastFmTrackApiService.getTopTracks(username, period);
        return "Your request is being processed :)";
    }

    @GetMapping("/library/{username}")
    public void getLibrary(@PathVariable String username) {
        lastFmArtistApiService.getLibraryArtists(username);
    }
}
