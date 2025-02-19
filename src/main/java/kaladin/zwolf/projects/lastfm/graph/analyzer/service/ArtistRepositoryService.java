package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
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

    public Optional<LastfmArtistInfo> findArtistByMbid(String mdid) {
        return artistRepository.findLastfmArtistInfoByArtist_Mbid(mdid);
    }

    public boolean saveArtistInfoIfNotExist(LastfmArtistInfo artistInfo) {
        String mbid = artistInfo.getArtist().getMbid();
        String name = artistInfo.getArtist().getName();
        if (mbid == null) {
            artistInfo.getArtist().setMbid(
                    UUID.nameUUIDFromBytes(name.getBytes()).toString()
            );
            mbid = artistInfo.getArtist().getMbid();
            log.info("Artist {} had null Mbid, setting it to {}", name, mbid);
        }
        var exists = findArtistByMbid(artistInfo.getArtist().getMbid());
        if (exists.isEmpty()) {
            setTags(artistInfo);
            artistRepository.save(artistInfo);
            log.info("ARTIST {} SAVED", mbid);
            return true;
        }
        log.warn("ARTIST {} ALREADY EXISTED IN DB", mbid);
        return false;
    }

    private void setTags(LastfmArtistInfo artistInfo) {
        var tags = artistInfo.getArtist().getTags().getTag();
        artistInfo.getArtist().getTags().setTag(tags.subList(0, Math.min(3, tags.size())));
    }
}
