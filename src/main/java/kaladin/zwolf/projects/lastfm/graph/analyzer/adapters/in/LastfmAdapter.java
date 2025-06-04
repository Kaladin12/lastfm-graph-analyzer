package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.in;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.LastFmArtistApiService;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.LastFmTrackApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/lastfm")
@CrossOrigin("*")
@EnableAsync
@Tag(name = "LastFm adapter")
public class LastfmAdapter {
    private final Logger log = LoggerFactory.getLogger(LastfmAdapter.class);

    private final LastFmArtistApiService lastFmArtistApiService;
    private final LastFmTrackApiService lastFmTrackApiService;

    public LastfmAdapter(LastFmArtistApiService lastFmArtistApiService, LastFmTrackApiService lastFmTrackApiService) {
        this.lastFmArtistApiService = lastFmArtistApiService;
        this.lastFmTrackApiService = lastFmTrackApiService;
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        log.info("RECEIVED TOKEN: {}", token);
        String sessionKey = lastFmArtistApiService.getSessionKey(token);
        log.info("RETRIEVED SESSION KEY: {}", sessionKey);
        response.addHeader("session-key", sessionKey);
        response.sendRedirect("https://www.youtube.com/");
    }

    @GetMapping("/artists/{id}")
    public void getArtist(@PathVariable String id) {
        lastFmArtistApiService.getArtistInfo(id).ifPresent(artist -> log.info("RETRIEVED ARTIST: {}", artist));
    }

    @GetMapping("/{username}/tracks/top")
    public String getTrackTop(@PathVariable String username, @RequestParam("period") String period) {
        lastFmTrackApiService.getTopTracks(username, period);
        return "Your request is being processed :)";
    }

    @GetMapping("/{username}/library")
    public ResponseEntity<String> getLibrary(@PathVariable String username) {
        lastFmArtistApiService.getLibraryArtists(username);
        return ResponseEntity.accepted().body("Your request is being processed :)");
    }

    @GetMapping("/{username}/library/artists/{id}")
    public void getLibraryArtist(@PathVariable String username, @PathVariable String id) {
        
    }
}
