package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmApiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
public class LastFmApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmApiService.class);

    private LastFmApiAdapter lastFmApiAdapter;

    public LastFmApiService(LastFmApiAdapter lastFmApiAdapter) {
        this.lastFmApiAdapter = lastFmApiAdapter;
    }

    public String getSessionKey(String token) {
        try {
            return Objects.requireNonNull(lastFmApiAdapter.getWebServiceSession(token).getBody())
                    .getSession().getKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to retrieve session key due to error: {}", e.getMessage());
        }
        return null;
    }

    public String getArtistInfo(String artistName) {
        return lastFmApiAdapter.getArtistInfo(artistName).getBody();
    }
}
