package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.ArtistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ArtistRepositoryService {
    private final Logger log = LoggerFactory.getLogger(ArtistRepositoryService.class);

    private ArtistRepository artistRepository;

    public ArtistRepositoryService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Optional<LastfmArtist> findArtistByMbid(String mdid) {
        return artistRepository.findLastfmArtistByMbid(mdid);
    }

    public void saveArtistInfoIfNotExist(LastfmArtist artistInfo) {
        String mbid = artistInfo.getMbid();
        String name = artistInfo.getName();
        if (mbid == null) {
            artistInfo.setMbid(
                    UUID.nameUUIDFromBytes(name.getBytes()).toString()
            );
            mbid = artistInfo.getMbid();
            log.debug("Artist {} had null Mbid, setting it to {}", name, mbid);
        }
        var exists = findArtistByMbid(artistInfo.getMbid());
        if (exists.isEmpty()) {
            setTags(artistInfo);
            artistRepository.save(artistInfo);
            log.debug("ARTIST {} SAVED", mbid);
            return;
        }
        log.warn("ARTIST {} ALREADY EXISTED IN DB", mbid);
    }

    private void setTags(LastfmArtist artistInfo) {
        var tags = artistInfo.getTags();
        artistInfo.setTags(tags.subList(0, Math.min(3, tags.size())));
    }
}
