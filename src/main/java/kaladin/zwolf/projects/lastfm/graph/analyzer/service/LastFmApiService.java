package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.repositories.ArtistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

@Service
public class LastFmApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmApiService.class);

    private LastFmApiAdapter lastFmApiAdapter;
    private ArtistRepository artistRepository;

    public LastFmApiService(LastFmApiAdapter lastFmApiAdapter, ArtistRepository artistRepository) {
        this.lastFmApiAdapter = lastFmApiAdapter;
        this.artistRepository = artistRepository;
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

    public LastfmArtistInfo getArtistInfo(String artistName) {
        LastfmArtistInfo artistInfo =  lastFmApiAdapter.getArtistInfo(artistName).getBody();
        var tags = artistInfo.getArtist().getTags().getTag();
        artistInfo.getArtist().getTags().setTag(tags.subList(0, Math.min(3, tags.size())));
        var exists = artistRepository.findLastfmArtistInfoByArtist_Mbid(artistInfo.getArtist().getMbid());
        if (exists.isEmpty()) {
            artistRepository.save(artistInfo);
            return artistInfo;
        }
        log.warn("ARTIST {} ALREADY EXISTED IN DB", artistInfo.getArtist().getMbid());
        return null;

    }
}
